package com.defano.hypertalk.ast.statement.command;

import com.defano.hypertalk.ast.expression.ListExp;
import com.defano.hypertalk.ast.expression.container.PartExp;
import com.defano.hypertalk.ast.model.specifier.PartSpecifier;
import com.defano.hypertalk.ast.statement.Statement;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import com.defano.wyldcard.message.Message;
import com.defano.wyldcard.message.MessageBuilder;
import com.defano.wyldcard.part.model.PartModel;
import com.defano.wyldcard.runtime.ExecutionContext;
import org.antlr.v4.runtime.ParserRuleContext;

public class MessageCmd extends Statement {

    private final Message message;
    private PartExp messageRecipient;

    public MessageCmd(ParserRuleContext context, PartExp messageRecipient, String messageName, ListExp messageArgs) {
        this(context, messageRecipient, MessageBuilder.named(messageName).withArguments(messageArgs).build());
    }

    public MessageCmd(ParserRuleContext context, String messageName, ListExp messageArgs) {
        this(context, null, messageName, messageArgs);
    }

    public MessageCmd(ParserRuleContext context, PartExp messageRecipient, Message message) {
        super(context);
        this.message = message;
        this.messageRecipient = messageRecipient;
    }

    @Override
    public void onExecute(ExecutionContext context) throws HtException {

        // Who are we sending the message to?
        PartSpecifier recipient = messageRecipient == null ?
                context.getStackFrame().getMe() :
                messageRecipient.evaluateAsSpecifier(context);

        // Find the model associated with that recipient
        PartModel recipientModel = context.getPart(recipient);
        if (recipientModel == null) {
            throw new HtSemanticException("No such message recipient.");
        }

        // Special case: Message is originating from message box; use unbound context when sending
        if (recipientModel.getParentStackModel() == null) {
            recipientModel.receiveMessage(context.unbind(), this, message);
        }

        // Typical case: One stack part is sending a message to another stack part
        else {
            recipientModel.blockingReceiveMessage(context.bindStack(recipientModel), this, message);
        }
    }
}
