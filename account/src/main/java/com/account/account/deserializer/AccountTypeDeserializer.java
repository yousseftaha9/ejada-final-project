package com.account.account.deserializer;

import com.account.account.enums.AccountType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class AccountTypeDeserializer extends JsonDeserializer<AccountType> {
    @Override
    public AccountType deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String value = p.getText();

        if (value == null || value.trim().isEmpty()) {
            throw new JsonProcessingException("Account type is required") {};
        }

        try {
            return AccountType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new JsonProcessingException("Invalid account type. Must be one of: " +
                    java.util.Arrays.toString(AccountType.values())) {};
        }
    }
}