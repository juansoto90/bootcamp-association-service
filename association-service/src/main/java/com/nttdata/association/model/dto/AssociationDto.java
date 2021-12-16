package com.nttdata.association.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssociationDto {
    private String cardNumber;
    private String cardType;
    private List<AccountDto> accountList;
}
