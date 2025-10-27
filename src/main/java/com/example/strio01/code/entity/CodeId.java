package com.example.strio01.code.entity;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CodeId implements Serializable {
    private String codeGroup;
    private String codeId;
}
