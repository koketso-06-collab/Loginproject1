package com.mycompany.loginproject1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    // Test Data for Task 1 from your photo
    Message message1 = new Message("0011223344", 0, "+27718693002", "Hi Mike, can you join us for dinner tonight?");

    // Test Data for Message 2
    Message message2 = new Message("0011223345", 1, "0857595889", "Hi Keegan, did you receive the payment?");

    @Test
    public void testRecipientNumberCorrectlyFormatted() {
        assertEquals("Cell phone number successfully captured.", message1.checkRecipientCell());
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.", message2.checkRecipientCell());
    }

    @Test
    public void testMessageHashIsCorrect() {
        // Should return 00:0:HITONIGHT for Test Case 1
        assertEquals("00:0:HITONIGHT", message1.createMessageHash());
        
        // Test remainder in loop
        Message m2 = new Message("0011223345", 1, "+27718693002", "Hi Keegan, did you receive the payment?");
        assertEquals("00:1:HIPAYMENT", m2.createMessageHash());
    }

    @Test
    public void testMessageLengthSuccessAndFailure() {
        assertEquals("Message ready to send.", message1.checkMessageLength());
        
        // Create long message >250
        String longMsg = "a".repeat(260);
        Message longMessage = new Message("1234567890", 0, "+27718693002", longMsg);
        assertTrue(longMessage.checkMessageLength().contains("Message exceeds 250 characters by 10"));
    }

    @Test
    public void testMessageIDCreated() {
        assertTrue(message1.checkMessageID());
        String idMsg = "Message ID generated: <" + message1.getMessageID() + ">";
        assertNotNull(idMsg);
        assertEquals(10, message1.getMessageID().length());
    }

    @Test
    public void testMessageSentOptions() {
        assertEquals("Message successfully sent.", message1.SentMessage("Send"));
        assertEquals("Press 0 to delete the message.", message1.SentMessage("Disregard"));
        assertEquals("Message successfully stored.", message1.SentMessage("Store"));
    }

    @Test
    public void testTotalMessages() {
        Message m = new Message("1234567890", 0, "+27718693002", "Test");
        m.SentMessage("Send");
        assertTrue(m.returnTotalMessages() >= 1);
    }
}