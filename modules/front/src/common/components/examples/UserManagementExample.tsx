import { useUsers, useCreateUser, useUpdateUser, useDeleteUser } from "@common/api";
import { useState } from "react";
import { Button } from "@common/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@common/components/ui/card";

export function UserManagementExample() {
  const { data: users, isLoading, error } = useUsers();
  const createUser = useCreateUser();
  const updateUser = useUpdateUser();
  const deleteUser = useDeleteUser();

  const [newUser, setNewUser] = useState({
    nom: "",
    prenom: "",
    email: "",
    motDePasse: "",
    photoProfil: "",
  });

  const handleCreateUser = async () => {
    try {
      await createUser.mutateAsync(newUser);
      setNewUser({
        nom: "",
        prenom: "",
        email: "",
        motDePasse: "",
        photoProfil: "",
      });
    } catch (error) {
      console.error("Erreur lors de la création de l'utilisateur:", error);
    }
  };

  const handleUpdateUser = async (uuid: string) => {
    try {
      await updateUser.mutateAsync({
        id: uuid,
        data: { nom: "Nom mis à jour" },
      });
    } catch (error) {
      console.error("Erreur lors de la mise à jour:", error);
    }
  };

  const handleDeleteUser = async (uuid: string) => {
    try {
      await deleteUser.mutateAsync(uuid);
    } catch (error) {
      console.error("Erreur lors de la suppression:", error);
    }
  };

  if (isLoading) return <div>Chargement...</div>;
  if (error) return <div>Erreur: {error.message}</div>;

  return (
    <div className="p-4 space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Gestion des Utilisateurs</CardTitle>
          <CardDescription>Exemple d'utilisation des hooks TanStack Query</CardDescription>
        </CardHeader>
        <CardContent>
          {/* Formulaire de création */}
          <div className="mb-6 p-4 border rounded-lg">
            <h3 className="text-lg font-semibold mb-4">Créer un nouvel utilisateur</h3>
            <div className="grid grid-cols-2 gap-4 mb-4">
              <input
                type="text"
                placeholder="Nom"
                value={newUser.nom}
                onChange={(e) => setNewUser({ ...newUser, nom: e.target.value })}
                className="border rounded px-3 py-2"
              />
              <input
                type="text"
                placeholder="Prénom"
                value={newUser.prenom}
                onChange={(e) => setNewUser({ ...newUser, prenom: e.target.value })}
                className="border rounded px-3 py-2"
              />
              <input
                type="email"
                placeholder="Email"
                value={newUser.email}
                onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                className="border rounded px-3 py-2"
              />
              <input
                type="password"
                placeholder="Mot de passe"
                value={newUser.motDePasse}
                onChange={(e) => setNewUser({ ...newUser, motDePasse: e.target.value })}
                className="border rounded px-3 py-2"
              />
            </div>
            <Button 
              onClick={handleCreateUser} 
              disabled={createUser.isPending}
            >
              {createUser.isPending ? "Création..." : "Créer"}
            </Button>
          </div>

          {/* Liste des utilisateurs */}
          <div className="space-y-2">
            <h3 className="text-lg font-semibold mb-4">Liste des utilisateurs ({users?.length || 0})</h3>
            {users?.map((user) => (
              <div key={user.uuid} className="flex items-center justify-between p-3 border rounded">
                <div>
                  <p className="font-medium">{user.prenom} {user.nom}</p>
                  <p className="text-sm text-gray-600">{user.email}</p>
                  <p className="text-xs text-gray-500">{user.role} - {user.statut}</p>
                </div>
                <div className="flex gap-2">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleUpdateUser(user.uuid)}
                    disabled={updateUser.isPending}
                  >
                    Modifier
                  </Button>
                  <Button
                    size="sm"
                    variant="destructive"
                    onClick={() => handleDeleteUser(user.uuid)}
                    disabled={deleteUser.isPending}
                  >
                    Supprimer
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
