-- =========================================================================
-- Version: 1.0.0
-- Date: 2026-03-05
-- Auteur: Équipe PayToGether
-- Description: Schéma initial de la base de données PayToGether
-- =========================================================================

-- Table Utilisateur
CREATE TABLE IF NOT EXISTS utilisateur (
    uuid UUID PRIMARY KEY,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    statut VARCHAR(50) NOT NULL DEFAULT 'INACTIF',
    role VARCHAR(50) NOT NULL,
    photo_profil_uuid UUID,
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE utilisateur IS 'Table des utilisateurs de l''application PayToGether';
COMMENT ON COLUMN utilisateur.statut IS 'Statut: ACTIF, INACTIF, SUSPENDU';
COMMENT ON COLUMN utilisateur.role IS 'Rôle: USER, ADMIN, MODERATOR';

-- Table Catégorie
CREATE TABLE IF NOT EXISTS categorie (
    uuid UUID PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE categorie IS 'Catégories de deals (ex: Électronique, Mode, Voyage)';

-- Table Deal
CREATE TABLE IF NOT EXISTS deal (
    uuid UUID PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    prix_deal DECIMAL(10, 2) NOT NULL,
    prix_part DECIMAL(10, 2) NOT NULL,
    nb_participants INTEGER NOT NULL,
    statut VARCHAR(50) NOT NULL DEFAULT 'BROUILLON',
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP NOT NULL,
    createur_uuid UUID NOT NULL,
    categorie_uuid UUID NOT NULL,
    ville VARCHAR(100) NOT NULL,
    pays VARCHAR(100) NOT NULL DEFAULT 'Canada',
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_deal_createur FOREIGN KEY (createur_uuid)
        REFERENCES utilisateur(uuid) ON DELETE CASCADE,
    CONSTRAINT fk_deal_categorie FOREIGN KEY (categorie_uuid)
        REFERENCES categorie(uuid) ON DELETE RESTRICT
);

COMMENT ON TABLE deal IS 'Deals créés par les utilisateurs';
COMMENT ON COLUMN deal.statut IS 'Statut: BROUILLON, PUBLIE, EXPIRE, ANNULE';

CREATE INDEX idx_deal_createur_uuid ON deal(createur_uuid);
CREATE INDEX idx_deal_categorie_uuid ON deal(categorie_uuid);
CREATE INDEX idx_deal_statut ON deal(statut);
CREATE INDEX idx_deal_date_fin ON deal(date_fin);

-- Table Points Forts Deal
CREATE TABLE IF NOT EXISTS point_fort_deal (
    uuid UUID PRIMARY KEY,
    deal_uuid UUID NOT NULL,
    texte VARCHAR(255) NOT NULL,
    ordre INTEGER NOT NULL DEFAULT 0,
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_point_fort_deal FOREIGN KEY (deal_uuid)
        REFERENCES deal(uuid) ON DELETE CASCADE
);

COMMENT ON TABLE point_fort_deal IS 'Points forts d''un deal (liste à puces)';

CREATE INDEX idx_point_fort_deal_uuid ON point_fort_deal(deal_uuid);

-- Table Image Deal
CREATE TABLE IF NOT EXISTS image_deal (
    uuid UUID PRIMARY KEY,
    deal_uuid UUID NOT NULL,
    url_image VARCHAR(500) NOT NULL,
    is_principal BOOLEAN DEFAULT FALSE,
    statut VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_image_deal FOREIGN KEY (deal_uuid)
        REFERENCES deal(uuid) ON DELETE CASCADE
);

COMMENT ON TABLE image_deal IS 'Images associées à un deal';
COMMENT ON COLUMN image_deal.statut IS 'Statut: PENDING, UPLOADED, FAILED';

CREATE INDEX idx_image_deal_uuid ON image_deal(deal_uuid);

-- Table Image Utilisateur
CREATE TABLE IF NOT EXISTS image_utilisateur (
    uuid UUID PRIMARY KEY,
    utilisateur_uuid UUID NOT NULL,
    url_image VARCHAR(500) NOT NULL,
    statut VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_image_utilisateur FOREIGN KEY (utilisateur_uuid)
        REFERENCES utilisateur(uuid) ON DELETE CASCADE
);

COMMENT ON TABLE image_utilisateur IS 'Photos de profil des utilisateurs';

CREATE INDEX idx_image_utilisateur_uuid ON image_utilisateur(utilisateur_uuid);

-- Table Publicité
CREATE TABLE IF NOT EXISTS publicite (
    uuid UUID PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    url_externe VARCHAR(500),
    statut VARCHAR(50) NOT NULL DEFAULT 'INACTIVE',
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP NOT NULL,
    createur_uuid UUID NOT NULL,
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_publicite_createur FOREIGN KEY (createur_uuid)
        REFERENCES utilisateur(uuid) ON DELETE CASCADE
);

COMMENT ON TABLE publicite IS 'Publicités affichées sur le site';
COMMENT ON COLUMN publicite.statut IS 'Statut: ACTIVE, INACTIVE';

CREATE INDEX idx_publicite_createur_uuid ON publicite(createur_uuid);
CREATE INDEX idx_publicite_statut ON publicite(statut);

-- Table Image Publicité
CREATE TABLE IF NOT EXISTS image_publicite (
    uuid UUID PRIMARY KEY,
    publicite_uuid UUID NOT NULL,
    url_image VARCHAR(500) NOT NULL,
    is_principal BOOLEAN DEFAULT FALSE,
    statut VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_image_publicite FOREIGN KEY (publicite_uuid)
        REFERENCES publicite(uuid) ON DELETE CASCADE
);

COMMENT ON TABLE image_publicite IS 'Images associées à une publicité';

CREATE INDEX idx_image_publicite_uuid ON image_publicite(publicite_uuid);

-- Table Commentaire
CREATE TABLE IF NOT EXISTS commentaire (
    uuid UUID PRIMARY KEY,
    deal_uuid UUID NOT NULL,
    utilisateur_uuid UUID NOT NULL,
    contenu TEXT NOT NULL,
    commentaire_parent_uuid UUID,
    est_pertinent BOOLEAN DEFAULT FALSE,
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_commentaire_deal FOREIGN KEY (deal_uuid)
        REFERENCES deal(uuid) ON DELETE CASCADE,
    CONSTRAINT fk_commentaire_utilisateur FOREIGN KEY (utilisateur_uuid)
        REFERENCES utilisateur(uuid) ON DELETE CASCADE,
    CONSTRAINT fk_commentaire_parent FOREIGN KEY (commentaire_parent_uuid)
        REFERENCES commentaire(uuid) ON DELETE CASCADE
);

COMMENT ON TABLE commentaire IS 'Commentaires des utilisateurs sur les deals';
COMMENT ON COLUMN commentaire.commentaire_parent_uuid IS 'UUID du commentaire parent pour les réponses';
COMMENT ON COLUMN commentaire.est_pertinent IS 'Flag pour marquer les commentaires pertinents/utiles';

CREATE INDEX idx_commentaire_deal_uuid ON commentaire(deal_uuid);
CREATE INDEX idx_commentaire_utilisateur_uuid ON commentaire(utilisateur_uuid);
CREATE INDEX idx_commentaire_parent_uuid ON commentaire(commentaire_parent_uuid);

-- Table Validation Token
CREATE TABLE IF NOT EXISTS validation_token (
    uuid UUID PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    utilisateur_uuid UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    date_expiration TIMESTAMP NOT NULL,
    est_utilise BOOLEAN DEFAULT FALSE,
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_validation_token_utilisateur FOREIGN KEY (utilisateur_uuid)
        REFERENCES utilisateur(uuid) ON DELETE CASCADE
);

COMMENT ON TABLE validation_token IS 'Tokens de validation pour email, réinitialisation mot de passe, etc.';
COMMENT ON COLUMN validation_token.type IS 'Type: EMAIL_VALIDATION, PASSWORD_RESET';

CREATE INDEX idx_validation_token_utilisateur ON validation_token(utilisateur_uuid);
CREATE INDEX idx_validation_token_token ON validation_token(token);

