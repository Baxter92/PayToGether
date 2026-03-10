package com.ulr.paytogether.provider.adapter.entity.elasticsearch;

import com.ulr.paytogether.core.enumeration.StatutDeal;
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
import java.util.UUID;

/**
 * Document Elasticsearch pour l'indexation des deals
 * Index: deals
 */
@Document(indexName = "deals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private UUID uuid;

    @Field(type = FieldType.Text, analyzer = "french")
    private String titre;

    @Field(type = FieldType.Text, analyzer = "french")
    private String description;

    @Field(type = FieldType.Double)
    private BigDecimal prixDeal;

    @Field(type = FieldType.Double)
    private BigDecimal prixPart;

    @Field(type = FieldType.Integer)
    private Integer nbParticipants;

    @Field(type = FieldType.Date)
    private LocalDateTime dateDebut;

    @Field(type = FieldType.Date)
    private LocalDateTime dateFin;

    @Field(type = FieldType.Keyword)
    private StatutDeal statut;

    @Field(type = FieldType.Text)
    private String ville;

    @Field(type = FieldType.Text)
    private String pays;

    @Field(type = FieldType.Keyword)
    private UUID categorieUuid;

    @Field(type = FieldType.Text)
    private String categorieNom;

    @Field(type = FieldType.Keyword)
    private UUID createurUuid;

    @Field(type = FieldType.Text)
    private String createurNom;

    @Field(type = FieldType.Text)
    private String imagePrincipaleUrl;

    @Field(type = FieldType.Integer)
    private int nombreDeVues;

    @Field(type = FieldType.Date)
    private LocalDateTime dateCreation;

    @Field(type = FieldType.Date)
    private LocalDateTime dateModification;
}

