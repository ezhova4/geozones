package com.clevertec.datatransfer.exception;

import com.clevertec.datatransfer.entity.Error;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class ErrorResponseFromWialonException extends Exception {
    private Error error;

    public ErrorResponseFromWialonException(String message, Error error) {
        super(message);
        this.error = error;
    }
}
