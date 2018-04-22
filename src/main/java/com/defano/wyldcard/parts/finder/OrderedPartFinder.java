package com.defano.wyldcard.parts.finder;

import com.defano.wyldcard.parts.PartException;
import com.defano.wyldcard.parts.card.CardLayer;
import com.defano.wyldcard.parts.card.CardLayerPartModel;
import com.defano.wyldcard.parts.model.PartModel;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.window.WindowManager;
import com.defano.hypertalk.ast.model.Ordinal;
import com.defano.hypertalk.ast.model.PartType;
import com.defano.hypertalk.ast.model.specifiers.*;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Provides functions for finding parts that exist in some relative order to one another (for example, cards in a stack
 * or buttons in z-order on a card).
 */
public interface OrderedPartFinder {

    /**
     * Gets all parts that should be searched in the order that they appear or are displayed. For buttons and fields,
     * this is their z-order; for cards or backgrounds this is their order in the stack.
     *
     * @return The list of parts held by this container in their logical display order.
     * @param context The execution context.
     */
    List<PartModel> getPartsInDisplayOrder(ExecutionContext context);

    /**
     * Finds any part returned by {@link #getPartsInDisplayOrder(ExecutionContext)} by ID, name, number, or ordinal. Can also find the
     * message box.
     *
     *
     * @param context The execution context.
     * @param ps A part specifier indicating the part to find.
     * @return The model of the found part.
     * @throws PartException Thrown if the requested part cannot be located.
     */
    default PartModel findPart(ExecutionContext context, PartSpecifier ps) throws PartException {
        return findPart(context, ps, getPartsInDisplayOrder(context));
    }

    /**
     * Finds any part by ID, name, number, or ordinal within an ordered collection of parts.
     *
     *
     * @param context The execution context.
     * @param ps The part specifier representing the part to fetch
     * @param parts The list of parts to search
     * @return The specified part
     * @throws PartException Thrown if no such part exists on this card.
     */
    default PartModel findPart(ExecutionContext context, PartSpecifier ps, List<PartModel> parts) throws PartException {
        PartModel foundPart;

        if (ps instanceof PartIdSpecifier) {
            foundPart = findPartById(context, (PartIdSpecifier) ps, parts);
        } else if (ps instanceof PartNameSpecifier) {
            foundPart = findPartByName(context, (PartNameSpecifier) ps, parts);
        } else if (ps instanceof PartNumberSpecifier) {
            foundPart = findPartByNumber(context, (PartNumberSpecifier) ps, parts);
        } else if (ps instanceof PartOrdinalSpecifier) {
            foundPart = findPartByOrdinal(context, (PartOrdinalSpecifier) ps, parts);
        } else if (ps instanceof PartMessageSpecifier) {
            foundPart = WindowManager.getInstance().getMessageWindow().getPartModel();
        } else if (ps instanceof CompositePartSpecifier) {
            throw new PartException("Can't find that.");
        } else {
            throw new IllegalArgumentException("Bug! Unimplemented PartSpecifier: " + ps);
        }

        // Special case: Field needs to be evaluated in the context of the current card
        if (foundPart instanceof CardLayerPartModel) {
            ((CardLayerPartModel) foundPart).setCurrentCardId(context.getCurrentCard().getId(context));
        }

        return foundPart;
    }

    /**
     * Calculates the number of a part relative to all other parts returned by {@link #getPartsInDisplayOrder(ExecutionContext)}.
     *
     * @param context The execution context.
     * @param part The model of the part whose number should be retrieved.
     * @return The number of the given part.
     */
    default long getPartNumber(ExecutionContext context, PartModel part) {
        return getPartNumber(context, part, getPartsInDisplayOrder(context));
    }

    /**
     * Calculates the number of a part relative to all other parts returned by {@link #getPartsInDisplayOrder(ExecutionContext)} and
     * which match the given part type.
     *
     *
     * @param context The execution context.
     * @param part The model of the part whose number should be retrieved.
     * @param ofType The type of part being included in the count.
     * @return The number of the request part and type.
     */
    default long getPartNumber(ExecutionContext context, PartModel part, PartType ofType) {
        int number = 0;
        for (PartModel thisPart : getPartsInDisplayOrder(context)) {
            if (thisPart.getType() == ofType) {
                number++;
            }

            if (thisPart.getId(context) == part.getId(context) && thisPart.getType() == ofType) {
                return number;
            }
        }

        throw new IllegalArgumentException("No such part on this card.");
    }


    /**
     * Gets the "number" of the specified part relative to all other parts in the same layer of a given collection of
     * parts.
     * <p>
     * A part number is, effectively, its z-order on the card. The number is a value between 1 and the value returned
     * by {@link ##getPartCount(PartType, CardLayer)}, inclusively.
     *
     *
     * @param context The execution context.
     * @param part The part whose number should be returned.
     * @return The number of this part
     */
    default long getPartNumber(ExecutionContext context, PartModel part, List<PartModel> parts) {
        int number = 0;

        for (PartModel thisPart : parts) {
            number++;
            if (thisPart.getId(context) == part.getId(context)) {
                return number;
            }
        }

        throw new IllegalArgumentException("No such part on this card.");
    }

    /**
     * Finds a part based on its ID within a given collection of parts.
     *
     *
     * @param context The execution context.
     * @param ps The specification of the part to find.
     * @return The specified part.
     * @throws PartException Thrown if no part can be found matching the specifier.
     */
    default PartModel findPartById(ExecutionContext context, PartIdSpecifier ps, List<PartModel> parts) throws PartException {
        Optional<PartModel> foundPart = parts.stream()
                .filter(p -> ps.getType() == null || p.getType() == ps.getType())
                .filter(p -> ps.getOwner() == null || p.getOwner() == ps.getOwner())
                .filter(p -> p.getId(context) == ps.getValue())
                .findFirst();

        if (foundPart.isPresent()) {
            return foundPart.get();
        } else {
            throw new PartException("No " + ps.getHyperTalkIdentifier(context) + " found.");
        }
    }

    /**
     * Finds a part based on its name within a given collection of parts.
     *
     *
     * @param context The execution context.
     * @param ps The specification of the part to find.
     * @return The specified part.
     * @throws PartException Thrown if no part can be found matching the specifier.
     */
    default PartModel findPartByName(ExecutionContext context, PartNameSpecifier ps, List<PartModel> parts) throws PartException {
        Optional<PartModel> foundPart = parts.stream()
                .filter(p -> ps.getType() == null || p.getType() == ps.getType())
                .filter(p -> ps.getOwner() == null || p.getOwner() == ps.getOwner())
                .filter(p -> p.getName(context).equalsIgnoreCase(ps.getValue()))
                .findFirst();

        if (foundPart.isPresent()) {
            return foundPart.get();
        } else {
            throw new PartException("No " + ps.getHyperTalkIdentifier(context) + " found.");
        }
    }

    /**
     * Finds a part based on its number within a given collection of parts.
     *
     *
     * @param context The execution context.
     * @param ps The specification of the part to find.
     * @return The specified part.
     * @throws PartException Thrown if no part can be found matching the specifier.
     */
    default PartModel findPartByNumber(ExecutionContext context, PartNumberSpecifier ps, List<PartModel> parts) throws PartException {
        List<PartModel> foundParts = parts.stream()
                .filter(p -> ps.getType() == null || p.getType() == ps.getType())
                .filter(p -> ps.getOwner() == null || p.getOwner() == ps.getOwner())
                .collect(Collectors.toList());

        int partIndex = (int) ps.getValue() - 1;

        if (partIndex >= foundParts.size() || partIndex < 0) {
            throw new PartException("No " + ps.getHyperTalkIdentifier(context) + " found.");
        } else {
            return foundParts.get(partIndex);
        }
    }

    /**
     * Finds a part based on ordinal (first, second... middle, last) within a given collection of parts.
     *
     *
     * @param context The execution context.
     * @param ps The specification of the part to find
     * @return The specified part
     * @throws PartException Thrown if no part can by found matching the specifier.
     */
    default PartModel findPartByOrdinal(ExecutionContext context, PartOrdinalSpecifier ps, List<PartModel> parts) throws PartException {
        List<PartModel> foundParts = parts.stream()
                .filter(p -> ps.getType() == null || p.getType() == ps.getType())
                .filter(p -> ps.getOwner() == null || p.getOwner() == ps.getOwner())
                .collect(Collectors.toList());

        int index = ((Ordinal) ps.getValue()).intValue() - 1;

        if (ps.getValue() == Ordinal.LAST) {
            index = foundParts.size() - 1;
        } else if (ps.getValue() == Ordinal.MIDDLE) {
            index = foundParts.size() / 2;
        } else if (ps.getValue() == Ordinal.ANY && foundParts.size() > 0) {
            index = new Random().nextInt(foundParts.size());
        }

        if (index < 0 || index >= foundParts.size()) {
            throw new PartException("No such " + ps.getHyperTalkIdentifier(context) + ".");
        } else {
            return foundParts.get(index);
        }
    }

}
