package com.ulr.paytogether.provider.adapter.entity.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Document Elasticsearch pour l'indexation des deals
 * Index: deals
 *
 * NOTE: Tous les UUID sont stockés en String pour éviter les erreurs de conversion Elasticsearch
 */
@Document(indexName = "deals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealDocument {

    @Id
    private String id; // UUID au format String

    @Field(type = FieldType.Text, analyzer = "french")
    private String titre;

    @Field(type = FieldType.Text, analyzer = "french")
    private String description;

    @Field(type = FieldType.Double)
    private BigDecimal prixDeal;

    @Field(type = FieldType.Double)
    private BigDecimal prixPart;

    @Field(type = FieldType.Double)
    private BigDecimal prixPartNonReel; // Prix réel de la part (optionnel)

    @Field(type = FieldType.Integer)
    private Integer nbParticipants;

    @Field(type = FieldType.Keyword)
    private String statut; // StatutDeal stocké en String

    @Field(type = FieldType.Text)
    private String ville;

    @Field(type = FieldType.Text)
    private String pays;

    @Field(type = FieldType.Keyword)
    private String categorieUuid; // UUID au format String

    @Field(type = FieldType.Text)
    private String categorieNom;

    @Field(type = FieldType.Keyword)
    private String createurUuid; // UUID au format String

    @Field(type = FieldType.Text)
    private String createurNom;

    @Field(type = FieldType.Text)
    private String imagePrincipaleUrl;

    @Field(type = FieldType.Integer)
    private int nombreDeVues;
}

