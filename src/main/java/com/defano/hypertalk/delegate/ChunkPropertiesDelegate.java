package com.defano.hypertalk.delegate;

import com.defano.hypertalk.util.Range;
import com.defano.hypertalk.util.RangeUtils;
import com.defano.wyldcard.part.field.FieldModel;
import com.defano.wyldcard.part.model.PartModel;
import com.defano.wyldcard.runtime.ExecutionContext;
import com.defano.hypertalk.ast.model.chunk.Chunk;
import com.defano.hypertalk.ast.model.chunk.CompositeChunk;
import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.ast.model.specifier.PartSpecifier;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;

public class ChunkPropertiesDelegate {

    private static final String PROP_TEXTSIZE = "textsize";
    private static final String PROP_TEXTFONT = "textfont";
    private static final String PROP_TEXTSTYLE = "textstyle";

    public static Value getProperty(ExecutionContext context, String property, Chunk chunk, PartSpecifier part) throws HtException {

        PartModel partModel = context.getPart(part);

        if (!(partModel instanceof FieldModel)) {
            throw new HtSemanticException("Can't get that property from this part.");
        }

        FieldModel fieldModel = (FieldModel) partModel;
        Range range = (chunk instanceof CompositeChunk) ?
                RangeUtils.getRange(context, partModel.getValue(context).toString(), (CompositeChunk) chunk) :
                RangeUtils.getRange(context, partModel.getValue(context).toString(), chunk);

        switch (property.toLowerCase()) {
            case PROP_TEXTSIZE:
                return fieldModel.getTextFontSize(context, range.start, range.length());
            case PROP_TEXTFONT:
                return fieldModel.getTextFontFamily(context, range.start, range.length());
            case PROP_TEXTSTYLE:
                return fieldModel.getTextFontStyle(context, range.start, range.length());
            default:
                throw new HtSemanticException("Can't get that property from this part.");
        }
    }

    public static void setProperty(ExecutionContext context, String property, Value value, Chunk chunk, PartSpecifier part) throws HtException {

        PartModel partModel = context.getPart(part);

        if (!(partModel instanceof FieldModel)) {
            throw new HtSemanticException("Can't set that property on this part.");
        }

        FieldModel fieldModel = (FieldModel) partModel;
        Range range = (chunk instanceof CompositeChunk) ?
                RangeUtils.getRange(context, partModel.getValue(context).toString(), (CompositeChunk) chunk) :
                RangeUtils.getRange(context, partModel.getValue(context).toString(), chunk);

        switch (property.toLowerCase()) {
            case PROP_TEXTSIZE:
                if (!value.isInteger()) {
                    throw new HtSemanticException("The value '" + value.toString() + "' is not a valid font size.");
                }
                fieldModel.setTextFontSize(context, range.start, range.length(), value);
                break;
            case PROP_TEXTFONT:
                fieldModel.setTextFontFamily(context, range.start, range.length(), value);
                break;
            case PROP_TEXTSTYLE:
                fieldModel.setTextFontStyle(context, range.start, range.length(), value);
                break;
            default:
                throw new HtSemanticException("Can't set that property on this part.");
        }
    }

}
