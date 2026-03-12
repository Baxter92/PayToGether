Parcours complet
*Rappel un deal = une commande*

- Deal créer
- ⁠Paiement d’un premier utilisateur alors commande créer avec statut en cours (jusqu’ici on connaît la cinématique)
- ⁠Paiement du dernier utilisateur alors deal statut compléter et commande statut complète
- ⁠Payout de DealTogether au vendeur alors commande en statut Payout
- ⁠il verra un bouton pour ajouter sa facture si la commande est au statut Payout
- ⁠quand il enverra sa facture alors commande passe en statut Invoice_Seller
- ⁠un événement verra si la commande est en statut invoice_seller pour envoyer les factures aux clients concernés
- ⁠si ya besoin de renvoi d’une facture pour x raison c’est un cas manuel actuellement on verra plus tard
- ⁠le bouton pour upload la facture côté marchand disparaît pour laisser la place à un bouton pour afficher la liste des clients avec un bouton valider si valider il peut revenir en arrière en ce moment la, le principe est qu’il vérifie le numéro de paiement reçu par mail aux clients et validé puis quand c’est fait un bouton général sera ajouter pour envoyer ses validations
- ⁠alors s’il ya encore des clients à valider le statut de la commande reste pareil avec les validations des clients déjà enregistré en bd
- ⁠si les validations sont complètes alors le statut de la commande passe à terminer 
