-- Script pour réinitialiser les événements PaymentInitiatedEvent en échec
-- Date: 7 mars 2026

-- 1. Voir les événements PaymentInitiatedEvent en échec
SELECT
    event_id,
    event_type,
    status,
    attempt_count,
    error_message,
    occurred_on,
    last_attempt_at
FROM event_record
WHERE event_type = 'PaymentInitiatedEvent'
  AND status IN ('FAILED', 'PENDING')
ORDER BY occurred_on DESC;

-- 2. Réinitialiser les événements PaymentInitiatedEvent en échec pour les retraiter
UPDATE event_record
SET
    status = 'PENDING',
    attempt_count = 0,
    error_message = NULL,
    consumer_handler = NULL,
    last_attempt_at = NULL
WHERE event_type = 'PaymentInitiatedEvent'
  AND status = 'FAILED';

-- 3. Vérifier le résultat
SELECT
    status,
    COUNT(*) as count
FROM event_record
WHERE event_type = 'PaymentInitiatedEvent'
GROUP BY status;

-- 4. (Optionnel) Supprimer complètement les événements en échec si le retraitement ne fonctionne pas
-- ATTENTION: Cela supprime définitivement les événements
/*
DELETE FROM event_record
WHERE event_type = 'PaymentInitiatedEvent'
  AND status = 'FAILED'
  AND occurred_on < NOW() - INTERVAL '1 hour';
*/

