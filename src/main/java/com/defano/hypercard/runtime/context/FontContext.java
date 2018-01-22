package com.defano.hypercard.runtime.context;

import com.defano.hypercard.fonts.FontUtils;
import com.defano.hypercard.fonts.TextStyleSpecifier;
import com.defano.hypertalk.ast.model.Value;
import com.google.common.collect.Sets;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

import java.awt.*;
import java.util.Collection;
import java.util.Set;


/**
 * Manages the text style selection context. This is notably more complicated than one would initially assume.
 *
 * Several HyperCard entities get text styling from this context: Buttons (when selected with the button tool), fields
 * (when selected by the field tool), text inside of a field (applies to the selected range of text), and the text
 * painted by the text tool.
 *
 * Some Java APIs deal with {@link Font} objects that support only plain, bold and italic styles; other APIs deal
 * in terms of {@link javax.swing.text.AttributeSet} and support a wider range of styles and formats. Some APIs
 * improperly implement certain styles (for example, {@link javax.swing.text.rtf.RTFEditorKit} does not correctly persist
 * sub/superscript styles).
 *
 * The Font and Style menus are used to change the current font selection, but they also indicate the font and style of
 * whatever text-containing entity was last focused. This produces a complicated observational pattern--text entities
 * observe selections from the menus, and the menus observe the style of focused text entities.
 *
 * Choosing a size, font family or style from a menu should change only the property selected. For example,
 * making a selection bold should not also change the font to whatever font is shown selected in the Font menu, nor
 * should choosing bold affect any other styles--like italic or underline--that may also be applied to the selection
 * at large, or to a subset of the selection.
 *
 * Note that the "plain" style is an unusual case: Typically, it represents the absence of any other style. But, when
 * focusing text with multiple styles the "Plain" menu item can appear checked alongside other styles.
 *
 * To solve these problems, this object maintains two collections of state: The state of the focused font style
 * (representing what should be check-marked in the menus), and the state of the last active user selection
 * (representing a choice made from a menu or font chooser dialog). Changes to the "selected" font styling update the
 * "focused" style, but changes to the "focused" style do not affect the "selected" style.
 */
public class FontContext {

    private final static FontContext instance = new FontContext();

    private final static String DEFAULT_FONT_FAMILY = "Arial";
    private final static String DEFAULT_FONT_STYLE = "plain";
    private final static int DEFAULT_FONT_STYLE_CONST = Font.PLAIN;
    private final static int DEFAULT_FONT_SIZE = 12;

    // Font, size and style of last-focused text element (focused element may contain a mix of sizes, fonts and styles)
    private final Subject<Set<Value>> focusedFontFamilyProvider = BehaviorSubject.createDefault(Sets.newHashSet(new Value(DEFAULT_FONT_FAMILY)));
    private final Subject<Set<Value>> focusedFontSizeProvider = BehaviorSubject.createDefault(Sets.newHashSet(new Value(DEFAULT_FONT_SIZE)));
    private final Subject<Boolean> focusedPlainProvider = BehaviorSubject.createDefault(true);
    private final Subject<Boolean> focusedBoldProvider = BehaviorSubject.createDefault(false);
    private final Subject<Boolean> focusedItalicProvider = BehaviorSubject.createDefault(false);
    private final Subject<Boolean> focusedUnderlineProvider = BehaviorSubject.createDefault(false);
    private final Subject<Boolean> focusedStrikethroughProvider = BehaviorSubject.createDefault(false);
    private final Subject<Boolean> focusedSuperscriptProvider = BehaviorSubject.createDefault(false);
    private final Subject<Boolean> focusedSubscriptProvider = BehaviorSubject.createDefault(false);

    // Font, size and style of last font, size and style chosen by the user from the menus or chooser dialog
    private final Subject<Value> selectedFontFamilyProvider = BehaviorSubject.createDefault(new Value(DEFAULT_FONT_FAMILY));
    private final Subject<Value> selectedFontSizeProvider = BehaviorSubject.createDefault(new Value(DEFAULT_FONT_SIZE));
    private final Subject<Value> selectedFontStyleProvider = BehaviorSubject.createDefault(new Value(DEFAULT_FONT_STYLE));

    // For JMonet use only; components should listen for and react to font, style and size changes individually.
    private final Subject<Font> paintFontProvider = BehaviorSubject.createDefault(FontUtils.getFontByNameStyleSize(DEFAULT_FONT_FAMILY, DEFAULT_FONT_STYLE_CONST, DEFAULT_FONT_SIZE));

    public static FontContext getInstance() {
        return instance;
    }

    private FontContext() {

        // Change in selected font should always change focused font
        selectedFontFamilyProvider.subscribe(value -> focusedFontFamilyProvider.onNext(Sets.newHashSet(value)));
        selectedFontSizeProvider.subscribe(value -> focusedFontSizeProvider.onNext(Sets.newHashSet(value)));
        selectedFontStyleProvider.subscribe(value -> setFocusedFontStyle(TextStyleSpecifier.fromFontStyle(value), false));

        // Change in selected font should update paint tool
        selectedFontFamilyProvider.subscribe(value -> paintFontProvider.onNext(FontUtils.getFontByNameStyleSize(String.valueOf(value), getFocusedTextStyle().getAwtFontStyle(), getFocusedTextStyle().getFontSize())));
        selectedFontStyleProvider.subscribe(value -> paintFontProvider.onNext(FontUtils.getFontByNameStyleSize(getFocusedTextStyle().getFontFamily(), TextStyleSpecifier.convertHyperTalkStyleToAwt(value), getFocusedTextStyle().getFontSize())));
        selectedFontSizeProvider.subscribe(value -> paintFontProvider.onNext(FontUtils.getFontByNameStyleSize(getFocusedTextStyle().getFontFamily(), getFocusedTextStyle().getAwtFontStyle(), value.integerValue())));
    }

    /**
     * Convenience method to set the selected font context given an AWT font object.
     * @param font The selected font.
     */
    public void setSelectedFont(Font font) {
        if (font != null) {
            TextStyleSpecifier tss = TextStyleSpecifier.fromFont(font);

            setSelectedFontFamily(tss.getFontFamily());
            setSelectedFontSize(tss.getFontSize());
            setSelectedFontStyle(tss.getHyperTalkStyle());
        }
    }

    /**
     * Set the focused font context given a {@link TextStyleSpecifier} object.
     * @param tss The text style specifier.
     */
    public void setFocusedTextStyle(TextStyleSpecifier tss) {
        if (tss != null) {
            focusedFontFamilyProvider.onNext(Sets.newHashSet(new Value(tss.getFontFamily())));
            focusedFontSizeProvider.onNext(Sets.newHashSet(new Value(tss.getFontSize())));

            focusedPlainProvider.onNext(tss.isPlain());
            focusedBoldProvider.onNext(tss.isBold());
            focusedItalicProvider.onNext(tss.isItalic());
            focusedUnderlineProvider.onNext(tss.isUnderline());
            focusedStrikethroughProvider.onNext(tss.isItalic());
            focusedSubscriptProvider.onNext(tss.isSubscript());
            focusedSuperscriptProvider.onNext(tss.isSubscript());
        }
    }

    /**
     * Sets the selected font size.
     * @param size The font size.
     */
    public void setSelectedFontSize(int size) {
        selectedFontSizeProvider.onNext(new Value(size));
    }

    /**
     * Toggle the given style from the current style selection, that is, add the style if it's not already part of the
     * selection or remove it, if it is.
     * @param style The (single) style to toggle.
     */
    public void toggleSelectedFontStyle(Value style) {
        TextStyleSpecifier tss = TextStyleSpecifier.fromFontStyle(getFocusedHyperTalkFontStyle());
        tss.toggleFontStyle(style);
        selectedFontStyleProvider.onNext(tss.getHyperTalkStyle());
    }

    public void setSelectedFontStyle(Value style) {
        selectedFontStyleProvider.onNext(style);
    }

    public void setSelectedFontFamily(String fontName) {
        selectedFontFamilyProvider.onNext(new Value(fontName));
    }

    public Value getSelectedFontFamily() {
        return selectedFontFamilyProvider.blockingFirst();
    }

    public Subject<Value> getSelectedFontFamilyProvider() {
        return selectedFontFamilyProvider;
    }

    public Subject<Value> getSelectedFontSizeProvider() {
        return selectedFontSizeProvider;
    }

    public Value getSelectedFontSize() {
        return selectedFontSizeProvider.blockingFirst();
    }

    public Subject<Value> getSelectedFontStyleProvider() {
        return selectedFontStyleProvider;
    }

    public Value getSelectedFontStyle() {
        return selectedFontStyleProvider.blockingFirst();
    }

    public Subject<Font> getPaintFontProvider() {
        return paintFontProvider;
    }

    public Subject<Set<Value>> getFocusedFontFamilyProvider() {
        return focusedFontFamilyProvider;
    }

    public Subject<Set<Value>> getFocusedFontSizeProvider() {
        return focusedFontSizeProvider;
    }

    public Subject<Boolean> getFocusedBoldProvider() {
        return focusedBoldProvider;
    }

    public Subject<Boolean> getFocusedItalicProvider() {
        return focusedItalicProvider;
    }

    public Subject<Boolean> getFocusedUnderlineProvider() {
        return focusedUnderlineProvider;
    }

    public Subject<Boolean> getFocusedStrikethroughProvider() {
        return focusedStrikethroughProvider;
    }

    public Subject<Boolean> getFocusedSuperscriptProvider() {
        return focusedSuperscriptProvider;
    }

    public Subject<Boolean> getFocusedSubscriptProvider() {
        return focusedSubscriptProvider;
    }

    public Subject<Boolean> getFocusedPlainProvider() {
        return focusedPlainProvider;
    }

    /**
     * Gets a "single" text style representing the current focus. Careful: This method is inherently lossy. The current
     * focused text may contain a mixture of fonts, sizes, and styles. The method reduces that selection to a single
     * font family, size, and set of styles.
     *
     * Useful for methods that need to reduce a composite style selection to a single value (for example, to be
     * displayed as default selections in a font-picker).
     *
     * @return A lossy, single-style representation of the focused text style.
     */
    public TextStyleSpecifier getFocusedTextStyle() {
        return TextStyleSpecifier.fromNameStyleSize(
                (Value) focusedFontFamilyProvider.blockingFirst().toArray()[0],
                getFocusedHyperTalkFontStyle(),
                (Value) focusedFontSizeProvider.blockingFirst().toArray()[0]
        );
    }

    /**
     * Gets a HyperTalk representation of the currently focused font style selection; a comma-separated list of
     * styles, for example 'bold, italic' or 'plain'.
     *
     * @return A HyperTalk representation of the focused font style
     */
    private Value getFocusedHyperTalkFontStyle() {
        TextStyleSpecifier tss = TextStyleSpecifier.fromFontStyle(new Value("plain"));
        tss.setBold(focusedBoldProvider.blockingFirst());
        tss.setItalic(focusedItalicProvider.blockingFirst());
        tss.setUnderline(focusedUnderlineProvider.blockingFirst());
        tss.setStrikeThrough(focusedStrikethroughProvider.blockingFirst());
        tss.setSubscript(focusedSubscriptProvider.blockingFirst());
        tss.setSuperscript(focusedSuperscriptProvider.blockingFirst());

        return tss.getHyperTalkStyle();
    }

    private void setFocusedFontStyle(TextStyleSpecifier tss, boolean includePlain) {
        focusedPlainProvider.onNext(tss.isPlain() || includePlain);
        focusedBoldProvider.onNext(tss.isBold());
        focusedItalicProvider.onNext(tss.isItalic());
        focusedUnderlineProvider.onNext(tss.isUnderline());
        focusedStrikethroughProvider.onNext(tss.isStrikeThrough());
        focusedSuperscriptProvider.onNext(tss.isSuperscript());
        focusedSubscriptProvider.onNext(tss.isSubscript());
    }

    public void setFocusedHyperTalkFontStyles(Collection<Value> values) {
        TextStyleSpecifier tss = TextStyleSpecifier.fromFontStyle(new Value("plain"));
        boolean includesPlain = false;

        for (Value thisStyleSet : values) {
            tss.appendStyle(thisStyleSet);
            includesPlain |= tss.isPlain();
        }

        setFocusedFontStyle(tss, includesPlain);
    }
}
