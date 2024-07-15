package com.ckxnhat.truyenhay.type.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorApiResponse {
    private String status;
    private String message;
}
