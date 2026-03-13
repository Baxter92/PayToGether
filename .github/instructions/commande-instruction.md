Parcours complet
Tous les mails seront en anglais, les templates de mail seront à faire en anglais aussi, les factures brefs tous en anglais comme documents et mail mais le code en français.

*Rappel un deal = une commande*

- Deal créer
- ⁠Paiement d’un premier utilisateur alors commande créer avec statut en cours (jusqu’ici on connaît la cinématique) déjà fait 
- ⁠Paiement du dernier utilisateur alors deal statut compléter et commande statut complète
- ⁠Payout de DealTogether au vendeur alors commande en statut Payout
- ⁠il verra un bouton pour ajouter sa facture si la commande est au statut Payout
- ⁠quand il enverra sa facture alors commande passe en statut Invoice_Seller
- ⁠un événement verra si la commande est en statut invoice_seller pour envoyer les factures aux clients concernés
- ⁠si ya besoin de renvoi d’une facture pour x raison c’est un cas manuel actuellement on verra plus tard
- ⁠le bouton pour upload la facture côté marchand disparaît pour laisser la place à un bouton pour afficher la liste des clients avec un bouton valider si valider il peut revenir en arrière en ce moment la, le principe est qu’il vérifie le numéro de paiement reçu par mail aux clients et validé puis quand c’est fait un bouton général sera ajouter pour envoyer ses validations
- ⁠alors s’il ya encore des clients à valider le statut de la commande reste pareil avec les validations des clients déjà enregistré en bd
- ⁠si les validations sont complètes alors le statut de la commande passe à terminer 

la librairie pour pdf est openhtmltopdf pour générer les factures et les envoyer par mail aux clients concernés.
On va rajouter une colonne sur l'entité Commande pour stocker la date de dépot du payout, ce qui permettra à l'admin de valider le payout et de déclencher les étapes suivantes du processus.
Tu vas créer en premier un endpoint avec en donnée la date de dépot du payout pour permettre à l'admin de valider un payout qui se traduira par le changement de statut de la commande en payout et l'envoi d'un mail au vendeur qui est lié au deal pour lui demander d'envoyer sa facture. Ensuite, tu créeras un endpoint pour permettre au vendeur d'uploader sa facture en multiformdata et l'enregistrer dans Minio avec le path `invoice/seller/`, ce qui changera le statut de la commande en invoice_seller et déclenchera, la création d'une facture par template avec comme donnée (numéro de la commande, le nom prenom du client, 'adresse du paiement , le deal concerné, le boolean homme deslivrey ou pickup à une adresse que je vais entrée en paramètre)
Pour les montant voici comment est composé le montant que paie le client enregistré dans la table de paiement : le montant total sera préalablement affiché, puis les détail comme format canada : 
```Java
double montantTrasanction = homePickup ? 12 : 0; // frais de transaction pour home delivery
double montantTotalFraisService = montantDuPaiement + 0.05 * montantDuPaiement;
double tva = 0.05 * montantTotalFraisService; // TVA de 5% sur le montant total des frais de service
double montantTotal = montantTotalFraisService + tva + montantTrasanction; // montant total à payer par le client
```
fais en sorte que le montant total soit affiché dans la facture générée pour le client, ainsi que les détails des frais de service, de la TVA et des frais de transaction si applicable.
A toi de déduire le montant (sous total), les frais de service, la TVA et les frais de transaction à partir du montant total enregistré dans la table de paiement pour afficher ces informations de manière claire et transparente dans la facture générée pour le client.
Ensuite, tu créeras un événement qui écoutera les commandes avec le statut invoice_seller
les factures seront générées en PDF et uploader dans Minio avec le path ìnvoice/user/, puis envoyées par mail aux clients concernés avec comme pièce jointe la facture générée. Le mail contiendra également un lien pour permettre au client de télécharger sa facture.
l'envoi des factures aux clients concernés. Enfin, tu créeras un endpoint pour permettre à l'admin de valider les factures des clients, ce qui changera le statut de la commande en terminé une fois que toutes les validations seront complètes. L'entité qui sera prise en compte est commandeUtilisateur qui contient les informations sur les clients concernés par la commande. Le processus de validation permettra à l'admin de vérifier les numéros de paiement reçus par mail aux clients et de valider ou non les factures en conséquence.


FRONTEND
- Lorsque le statut de la commande est en payout, un bouton "Upload Invoice" sera affiché pour permettre à l'admin d'ajouter la date de dépot ou d'envoie de l'argent au vendeur : 
* Ouverture d'un modal pour saisir la date de dépot du payout
* Envoi de la date de dépot du payout à l'endpoint créé pour valider le payout
- Lorsque le statut de la commande est en invoice_seller, un bouton "Upload Invoice" sera affiché pour permettre au vendeur d'uploader sa facture :
* Ouverture d'un modal pour uploader la facture en multiformdata
* Envoi de la facture à l'endpoint créé pour uploader la facture du vendeur
- Lorsque le statut de la commande est en invoice_customer, un bouton "Validate Invoice" sera affiché pour permettre au vendeur de valider les factures des clients concernés :
- * Ouverture d'un modal affichant la liste des clients concernés avec un bouton "Validate" à côté de chaque client
* Les validations peuvent être appliquer et retirer avec un `useState`, un bouton "Valider vos validations" sera affiché pour permettre au vendeur de valider les factures des clients concernés, 
* Si les validations sont complètes alors le statut de la commande passe à terminer et le bouton "Valider vos validations" disparaît pour laisser la place à un message indiquant que la commande est terminée. Si les validations ne sont pas complètes, le statut de la commande reste en invoice_customer et le vendeur peut continuer à valider les factures des clients concernés jusqu'à ce que toutes les validations soient complètes, mais il ne pourra pas changer le statut des client préalablement validé.
* Quand le statut de la commande est à invoice_customer ou terminer, l'admin peut consulter les validation des clients sans toute fois interagir avec les validations déjà appliquées, il peut seulement voir la liste des clients concernés et les validations appliquées pour chaque client.

La liste des commandes côté admin est le composant `AdminOrders` et la route `/admin/orders`. Tu peux ajouter les boutons et les modals dans ce composant pour permettre à l'admin de gérer les différentes étapes du processus de commande en fonction du statut de chaque commande. Assure-toi que les boutons et les modals sont affichés de manière conditionnelle en fonction du statut de la commande pour garantir une expérience utilisateur fluide et intuitive, avec un ux beau et propre.
La liste des commandes côté vendeur est le composant `HeaderProfile.tsx`, dans index tu peux voir les tabs et l'ajouter comme tabs uniquement pour le vendeur  et la route `/profile`. Tu peux ajouter les boutons et les modals dans ce composant pour permettre au vendeur de gérer les différentes étapes du processus de commande en fonction du statut de chaque commande. Assure-toi que les boutons et les modals sont affichés de manière conditionnelle en fonction du statut de la commande pour garantir une expérience utilisateur fluide et intuitive, avec un ux beau et propre.
Tu rajoute un bouton dans profile visible uniqmement par l'admin pour aller vers la route `/admin`, pour accéder au dashboard admin et gérer les commandes, les deals, les utilisateurs etc... Tu peux réutiliser le composant `AdminOrders` pour afficher la liste des commandes dans le dashboard admin et ajouter les fonctionnalités de gestion des commandes en fonction du statut de chaque commande. Assure-toi que le dashboard admin est accessible uniquement par les utilisateurs ayant le rôle d'admin pour garantir la sécurité de l'application.
Dans détail du deal, le bouton partager le deal change l'icone et permet le partage par mail ou autres du lien de détail du deal en question, commente également le bouton like qui n'est pas utilisé rends l'ux beau et fluide