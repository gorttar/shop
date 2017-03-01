package helpers.exceptions;/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-03-02)
 */
public class TestRuntimeException extends RuntimeException {
    public TestRuntimeException(String message) {
        super(message);
    }
}