import type { ReactElement } from "react";
import { useState } from "react";
import {
  FolderTree,
  Plus,
  MoreHorizontal,
  Pencil,
  Trash2,
  GripVertical,
  X,
} from "lucide-react";
import { Button } from "@/common/components/ui/button";
import { Badge } from "@/common/components/ui/badge";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/common/components/ui/card";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/common/components/ui/dropdown-menu";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/common/components/ui/dialog";
import Form from "@/common/containers/Form";
import { z } from "zod";
import { toast } from "sonner";
import { categories } from "@/common/constants/data";

const categorySchema = z.object({
  name: z.string().min(1, "Nom requis").max(50, "Nom trop long"),
  icon: z.string().min(1, "Icône requise").max(4, "Icône invalide"),
});

type CategoryFormData = z.infer<typeof categorySchema>;

export default function AdminCategories(): ReactElement {
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const handleCreateCategory = async ({ data }: { data: CategoryFormData }) => {
    console.log("New category:", data);
    toast.success("Catégorie créée", {
      description: `La catégorie "${data.name}" a été créée`,
    });
    setIsDialogOpen(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-heading font-bold flex items-center gap-2">
            <FolderTree className="h-8 w-8" />
            Catégories
          </h1>
          <p className="text-muted-foreground mt-1">
            Gérez les catégories de deals
          </p>
        </div>

        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <DialogTrigger asChild>
            <Button leftIcon={<Plus className="h-4 w-4" />}>
              Nouvelle catégorie
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Créer une catégorie</DialogTitle>
              <DialogDescription>
                Ajoutez une nouvelle catégorie pour organiser vos deals.
              </DialogDescription>
            </DialogHeader>
            <Form<CategoryFormData>
              schema={categorySchema}
              defaultValues={{ name: "", icon: "" }}
              onSubmit={handleCreateCategory}
              submitLabel="Créer"
              resetLabel="Annuler"
              submitBtnProps={{
                leftIcon: <Plus className="h-4 w-4" />,
              }}
              resetBtnProps={{
                leftIcon: <X className="h-4 w-4" />,
                onClick: () => setIsDialogOpen(false),
              }}
              columns={1}
              fields={[
                {
                  name: "name",
                  label: "Nom de la catégorie",
                  type: "text",
                  placeholder: "Ex: Restaurants",
                },
                {
                  name: "icon",
                  label: "Icône",
                  type: "text",
                },
              ]}
            />
          </DialogContent>
        </Dialog>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Total catégories</CardDescription>
            <CardTitle className="text-2xl">{categories.length}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Actives</CardDescription>
            <CardTitle className="text-2xl text-green-600">
              {categories.filter((c) => c.isActive).length}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Total deals</CardDescription>
            <CardTitle className="text-2xl">
              {categories.reduce((acc, c) => acc + c.count, 0)}
            </CardTitle>
          </CardHeader>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Liste des catégories</CardTitle>
          <CardDescription>
            Glissez-déposez pour réorganiser l'ordre d'affichage
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            {categories.map((category) => {
              const Icon = category.icon;
              return (
                <div
                  key={category.id}
                  className="flex items-center gap-4 p-4 border rounded-lg bg-card hover:bg-muted/50 transition-colors"
                >
                  <GripVertical className="h-5 w-5 text-muted-foreground cursor-grab" />

                  <span className="text-2xl">
                    <Icon />
                  </span>

                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="font-medium">{category.name}</span>
                      {!category.isActive && (
                        <Badge colorScheme="secondary">Inactive</Badge>
                      )}
                    </div>
                    <span className="text-sm text-muted-foreground">
                      /{category.slug}
                    </span>
                  </div>

                  <div className="text-right">
                    <span className="font-medium">{category.count}</span>
                    <span className="text-sm text-muted-foreground ml-1">
                      deals
                    </span>
                  </div>

                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="icon">
                        <MoreHorizontal className="h-4 w-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem>
                        <Pencil className="mr-2 h-4 w-4" />
                        Modifier
                      </DropdownMenuItem>
                      <DropdownMenuItem className="text-destructive">
                        <Trash2 className="mr-2 h-4 w-4" />
                        Supprimer
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              );
            })}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
