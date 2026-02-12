import { useDeals, useDealsByCategorie, useDealsByStatut, useCreateDeal } from "@common/api";
import { useState } from "react";
import { Button } from "@common/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@common/components/ui/card";
import { Badge } from "@common/components/ui/badge";

export function DealManagementExample() {
  const { data: allDeals, isLoading: isLoadingAll } = useDeals();
  const { data: publishedDeals, isLoading: isLoadingPublished } = useDealsByStatut("PUBLIE");
  const createDeal = useCreateDeal();

  const [selectedCategorie, setSelectedCategorie] = useState("");
  const { data: dealsByCategorie, isLoading: isLoadingByCategorie } = useDealsByCategorie(selectedCategorie);

  const [newDeal, setNewDeal] = useState({
    titre: "",
    description: "",
    prixDeal: 0,
    prixPart: 0,
    nbParticipants: 0,
    dateDebut: "",
    dateFin: "",
    dateExpiration: "",
    statut: "PUBLIE" as const,
    createurUuid: "",
    categorieUuid: "",
    ville: "",
    pays: "",
    listeImages: [] as string[],
    listePointsForts: [] as string[],
  });

  const handleCreateDeal = async () => {
    try {
      await createDeal.mutateAsync(newDeal);
      setNewDeal({
        titre: "",
        description: "",
        prixDeal: 0,
        prixPart: 0,
        nbParticipants: 0,
        dateDebut: "",
        dateFin: "",
        dateExpiration: "",
        statut: "PUBLIE",
        createurUuid: "",
        categorieUuid: "",
        ville: "",
        pays: "",
        listeImages: [],
        listePointsForts: [],
      });
    } catch (error) {
      console.error("Erreur lors de la création du deal:", error);
    }
  };

  if (isLoadingAll) return <div>Chargement des deals...</div>;

  return (
    <div className="p-4 space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Gestion des Deals</CardTitle>
          <CardDescription>Exemple d'utilisation des hooks TanStack Query pour les deals</CardDescription>
        </CardHeader>
        <CardContent>
          {/* Statistiques */}
          <div className="grid grid-cols-3 gap-4 mb-6">
            <Card>
              <CardContent className="pt-4">
                <p className="text-2xl font-bold">{allDeals?.length || 0}</p>
                <p className="text-sm text-gray-600">Total Deals</p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="pt-4">
                <p className="text-2xl font-bold">{publishedDeals?.length || 0}</p>
                <p className="text-sm text-gray-600">Deals Publiés</p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="pt-4">
                <p className="text-2xl font-bold">{dealsByCategorie?.length || 0}</p>
                <p className="text-sm text-gray-600">Par Catégorie</p>
              </CardContent>
            </Card>
          </div>

          {/* Filtres */}
          <div className="mb-6">
            <h3 className="text-lg font-semibold mb-2">Filtres</h3>
            <div className="flex gap-4">
              <Button
                variant={selectedCategorie === "" ? "default" : "outline"}
                onClick={() => setSelectedCategorie("")}
              >
                Toutes les catégories
              </Button>
              <Button
                variant={selectedCategorie !== "" ? "default" : "outline"}
                onClick={() => setSelectedCategorie("123e4567-e89b-12d3-a456-426614174000")}
              >
                Filtrer par catégorie
              </Button>
            </div>
          </div>

          {/* Liste des deals */}
          <div className="space-y-2">
            <h3 className="text-lg font-semibold mb-4">
              Deals ({selectedCategorie ? "par catégorie" : "tous"})
            </h3>
            {(selectedCategorie ? dealsByCategorie : allDeals)?.map((deal) => (
              <Card key={deal.uuid} className="p-4">
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <h4 className="font-semibold text-lg">{deal.titre}</h4>
                    <p className="text-gray-600 mb-2">{deal.description}</p>
                    <div className="flex gap-2 mb-2">
                      <Badge variant="secondary">{deal.categorieNom}</Badge>
                      <Badge variant="outline">{deal.statut}</Badge>
                      <Badge variant="default">{deal.ville}</Badge>
                    </div>
                    <div className="flex gap-4 text-sm">
                      <span>Prix deal: <strong>{deal.prixDeal}€</strong></span>
                      <span>Prix part: <strong>{deal.prixPart}€</strong></span>
                      <span>Participants: <strong>{deal.nbParticipants}</strong></span>
                    </div>
                    <p className="text-xs text-gray-500 mt-2">
                      Du {new Date(deal.dateDebut).toLocaleDateString()} au {new Date(deal.dateFin).toLocaleDateString()}
                    </p>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}