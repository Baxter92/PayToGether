Nous ajoutons le pattern event dispatcher dans un  module appelé event où toutes fonctions handler et model event seront ajouter et communiquerons avec le module core avec les évènements seront enregistrer en bd sous forme classe#évènement tables producer et consumer et chaque évènement sera prêt à être consomer par toutes les classes handlers qui auront une interface Functional permettant de faire 3 retry comme maxAttempt, 
Aussi un module appelé even-dispatcher avec les méthodes permettant les méthodes permettant d'appliquer les méthodes dispatch et consumer

En gros je dois publier l'évènement avec un dispatch event de l'object et tous les consumers héritent de l'interface handleer Functional avec un retry de max 3 attempts pouvant consommer l'évènement si c'est l'obect en question par exemple 

Toutes les classes handlers auront un sufixe handler et les évènements auront un sufixe event. c'est dans le module core que les évènement seront publier et dans le module event que les handlers seront créer pour consommer les évènements publiés par le module core
Toutes évèvements publié par le module core seront enregistré en base de données avec le nom de la classe et le nom de l'évènement pour que les handlers puissent consommer les évènements en fonction de leur type et de leur classe d'origine
Toutes les méthodes handlers auront une annotation @FunctionalHandler pour indiquer qu'elles sont des handlers fonctionnels qui peuvent consommer les évènements publiés par le module core et chaque handler aura une logique de retry pour gérer les échecs de consommation des évènements avec un maxAttempt de 3
Lorsque un évènement sera consomé par un handler, il sera marqué comme consommé en base de données pour éviter les consommations multiples et les handlers pourront consommer les évènements en fonction de leur type et de leur classe d'origine pour assurer une bonne organisation et une bonne gestion des évènements dans le système
lorsque les 3 retry seront atteints sans succès, l'évènement sera marqué comme échoué en base de données pour permettre une analyse ultérieure et une résolution des problèmes éventuels liés à la consommation des évènements
Chaque méthode handlers retourne un void et prends en paramètre l'object évènement qui contient les données nécessaires à la consommation de l'évènement, et les handlers peuvent effectuer les actions nécessaires en fonction des données de l'évènement pour assurer une bonne réactivité et une bonne gestion des évènements dans le système


```java
public class PaymentHandler implements ConsummerHandler {
    private final User user;
    private final PaymentService paymentService;
    @FunctionalHandler
    public void makePayment(ReceiveEvent event) {
        paymentService.makePayment(user, event.getData());
    }
}
```
Logique de publication d'un évènement dans le module core :
```java
public class PaymentServiceImpl implements PaymentService {
    private final EventDispatcher eventDispatcher;
    public void makePayment(User user, PaymentData data) {
        // Logic to make payment
        eventDispatcher.dispatch(new PaymentMadeEvent(user, data));
    }
}
```
Le module event n'implémente que le dispatch et les consumers que le module event-dispatcher implémentera.
Si on décide plustard d'impémenter une nouvelle logique de message listenner comme kafka; notre module event pourra être réutilisé sans modification, il suffira d'implémenter une nouvelle classe dans le module event-dispatcher pour consommer les évènements publiés par le module core.

