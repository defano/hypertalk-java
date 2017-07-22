/*
 * AbstractTextPaneField
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:10 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

package com.defano.hypercard.parts.fields.styles;

import com.defano.hypercard.context.GlobalContext;
import com.defano.hypercard.context.ToolMode;
import com.defano.hypercard.context.ToolsContext;
import com.defano.hypercard.parts.ToolEditablePart;
import com.defano.hypercard.parts.fields.FieldView;
import com.defano.hypercard.parts.model.FieldModel;
import com.defano.hypercard.parts.model.PartModel;
import com.defano.hypertalk.ast.common.Value;
import com.defano.jmonet.tools.util.MarchingAnts;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

public abstract class AbstractTextField extends JScrollPane implements FieldView, DocumentListener, Observer {

    protected final HyperCardJTextPane textPane;

    private ToolEditablePart toolEditablePart;
    private MutableAttributeSet currentStyle = new SimpleAttributeSet();

    public AbstractTextField(ToolEditablePart toolEditablePart) {
        this.toolEditablePart = toolEditablePart;

        // Create the editor component
        textPane = new HyperCardJTextPane(new DefaultStyledDocument());
        textPane.setEditorKit(new RTFEditorKit());
        this.setViewportView(textPane);

        // Add mouse and keyboard listeners
        this.addMouseListener(toolEditablePart);
        this.addKeyListener(toolEditablePart);
        textPane.addMouseListener(toolEditablePart);
        textPane.addKeyListener(toolEditablePart);

        // Get notified when field tool is selected
        ToolsContext.getInstance().getToolModeProvider().addObserver((o, arg) -> {
            setHorizontalScrollBarPolicy(ToolMode.FIELD == arg ? ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER : ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            setVerticalScrollBarPolicy(ToolMode.FIELD == arg ? ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER : ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            setEditable(ToolMode.FIELD != arg && !toolEditablePart.getPartModel().getKnownProperty(FieldModel.PROP_LOCKTEXT).booleanValue());
        });

        // Listen for changes to the field's selected text (for the selectedText property)
        textPane.addCaretListener(e -> toolEditablePart.getPartModel().defineProperty(PartModel.PROP_SELECTEDTEXT, textPane.getSelectedText() == null ? new Value() : new Value(textPane.getSelectedText()), true));
        textPane.addCaretListener(e -> GlobalContext.getContext().setSelectedText(textPane.getSelectedText() == null ? new Value() : new Value(textPane.getSelectedText())));

        // Listen for caret location changes so that we can update the app font selection
        textPane.addCaretListener(e -> {
            ToolsContext.getInstance().getFontProvider().deleteObserver(AbstractTextField.this);
            AttributeSet caretAttributes = textPane.getStyledDocument().getCharacterElement(e.getMark()).getAttributes();
            ToolsContext.getInstance().getFontProvider().set(textPane.getStyledDocument().getFont(caretAttributes));
            ToolsContext.getInstance().getFontProvider().addObserver(AbstractTextField.this);
        });

        // Listen for changes to the field's contents
        textPane.getStyledDocument().addDocumentListener(this);

        // And listen for ants to march
        MarchingAnts.getInstance().addObserver(this::repaint);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        FieldModel model = (FieldModel) toolEditablePart.getPartModel();
        model.setKnownProperty(FieldModel.PROP_TEXT, new Value(getText()), true);
        model.setStyleData(getRtf());
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        FieldModel model = (FieldModel) toolEditablePart.getPartModel();
        model.setKnownProperty(FieldModel.PROP_TEXT, new Value(getText()), true);
        model.setStyleData(getRtf());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        FieldModel model = (FieldModel) toolEditablePart.getPartModel();
        model.setKnownProperty(FieldModel.PROP_TEXT, new Value(getText()), true);
        model.setStyleData(getRtf());
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        toolEditablePart.drawSelectionRectangle(g);
    }

    @Override
    public void onPropertyChanged(String property, Value oldValue, Value newValue) {
        SwingUtilities.invokeLater(() -> {

            switch (property) {
                case FieldModel.PROP_TEXT:
                    if (!newValue.toString().equals(getText())) {
                        replaceText(newValue.stringValue());
                    }
                    break;

                case FieldModel.PROP_DONTWRAP:
                    textPane.setWrapText(!newValue.booleanValue());
                    break;

                case FieldModel.PROP_LOCKTEXT:
                    textPane.setEditable(!newValue.booleanValue());
                    break;

                case FieldModel.PROP_SHOWLINES:
                    textPane.setShowLines(newValue.booleanValue());
                    break;

                case FieldModel.PROP_TEXTSIZE:
                case FieldModel.PROP_TEXTSTYLE:
                case FieldModel.PROP_TEXTFONT:
                    updateActiveFieldStyle(toolEditablePart.getPartModel().getFont());
                    break;

                case FieldModel.PROP_ENABLED:
                    setEditable(newValue.booleanValue());
                    break;
            }
        });
    }

    /**
     * Replaces the field's text with the given value, attempting as best as possible to intelligently
     * maintain the field's existing style.
     *
     * It is not possible to correctly restyle the new text in every case. This is a result of the {@link FieldModel}
     * not being able to notify us of insert/delete operations.
     *
     * This method invokes Google's DiffMatchPatch utility to generate a change set, then attempts to apply each change
     * independently to best preserve formatting.
     *
     * TODO: Change much infrastructure to allow model to report inserts and deletes instead of using this "cheat".
     *
     * @param newText The text with which to replace the field's existing contents.
     */
    private void replaceText(String newText) {

        int changePosition = 0;
        String existingText = getText();
        StyledDocument document = textPane.getStyledDocument();
        AttributeSet style = currentStyle;

        // Do not perform incremental model update while we update
        document.removeDocumentListener(this);

        try {
            for (DiffMatchPatch.Diff thisDiff : getTextDifferences(existingText, newText)) {
                switch (thisDiff.operation) {
                    case EQUAL:
                        style = document.getCharacterElement(changePosition).getAttributes();
                        changePosition += thisDiff.text.length();
                        break;
                    case DELETE:
                        style = document.getCharacterElement(changePosition).getAttributes();
                        document.remove(changePosition, thisDiff.text.length());
                        break;
                    case INSERT:
                        document.insertString(changePosition, thisDiff.text, style);
                        changePosition += thisDiff.text.length();
                        break;
                }
            }
        } catch (BadLocationException e) {
            throw new RuntimeException("An error occurred updating field text.", e);
        } finally {
            document.addDocumentListener(this);
        }
    }

    @Override
    public String getText() {
        try {
            String text = textPane.getStyledDocument().getText(0, textPane.getStyledDocument().getLength());
            return (text == null) ? "" : text;
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JTextComponent getTextComponent() {
        return textPane;
    }

    @Override
    public void setEditable(boolean editable) {
        super.setEnabled(editable);
        textPane.setEnabled(editable);
    }

    @Override
    public void partOpened() {
        toolEditablePart.getPartModel().notifyPropertyChangedObserver(this);
        ToolsContext.getInstance().getToolModeProvider().notifyObservers(this);
        setRtf(((FieldModel) toolEditablePart.getPartModel()).getStyleData());
    }

    @Override
    public void partClosed() {
        ToolsContext.getInstance().getFontProvider().deleteObserver(this);
        ToolsContext.getInstance().getToolModeProvider().deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        // User changed global font selection from the menubar
        toolEditablePart.getPartModel().setFont((Font) arg);
    }

    private LinkedList<DiffMatchPatch.Diff> getTextDifferences(String existing, String replacement) {
        DiffMatchPatch dmp = new DiffMatchPatch();

        LinkedList<DiffMatchPatch.Diff> diffs = dmp.diffMain(existing, replacement);
        dmp.diffCleanupSemantic(diffs);

        return diffs;
    }

    private void setRtf(byte[] rtfData) {

        if (rtfData != null && rtfData.length != 0) {
            ByteArrayInputStream bais = new ByteArrayInputStream(rtfData);
            try {
                StyledDocument doc = new DefaultStyledDocument();

                textPane.getEditorKit().read(bais, doc, 0);
                bais.close();

                textPane.setStyledDocument(doc);
                doc.addDocumentListener(this);

                // RTFEditorKit appears to (erroneously) append a newline when we deserialize; get rid of that.
                textPane.getStyledDocument().remove(doc.getLength() - 1, 1);

            } catch (IOException | BadLocationException e) {
                throw new RuntimeException("An error occurred while restoring field contents.", e);
            }
        }
    }

    private byte[] getRtf() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = textPane.getStyledDocument();
            textPane.getEditorKit().write(baos, doc, 0, doc.getLength());
            baos.close();

            return baos.toByteArray();

        } catch (IOException | BadLocationException e) {
            throw new RuntimeException("An error occurred while saving field contents.", e);
        }
    }

    private void updateActiveFieldStyle(Font font) {
        currentStyle = new SimpleAttributeSet();
        currentStyle.addAttribute(StyleConstants.FontFamily, font.getFamily());
        currentStyle.addAttribute(StyleConstants.Size, font.getSize());
        currentStyle.addAttribute(StyleConstants.Bold, font.isBold());
        currentStyle.addAttribute(StyleConstants.Italic, font.isItalic());

        textPane.setCharacterAttributes(currentStyle, true);
    }
}
