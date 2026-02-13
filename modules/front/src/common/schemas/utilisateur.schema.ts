import { z } from "zod";
import { RoleUtilisateur, StatutUtilisateur } from "@/common/api/types";

export const createUtilisateurSchema = z.object({
  nom: z.string().min(1, "Le nom est obligatoire"),
  prenom: z.string().min(1, "Le prenom est obligatoire"),
  email: z.string().email("Email invalide"),
  motDePasse: z.string().min(1, "Le mot de passe est obligatoire"),
  photoProfil: z.string().optional(),
});

// Le backend valide UtilisateurDTO sur PUT:
// email, motDePasse et role sont requis.
export const updateUtilisateurSchema = z.object({
  nom: z.string().optional(),
  prenom: z.string().optional(),
  email: z.string().email("Email invalide"),
  motDePasse: z.string().min(1, "Le mot de passe est obligatoire"),
  statut: z
    .enum([StatutUtilisateur.ACTIF, StatutUtilisateur.INACTIF])
    .optional(),
  role: z.enum([
    RoleUtilisateur.UTILISATEUR,
    RoleUtilisateur.VENDEUR,
    RoleUtilisateur.ADMIN,
  ]),
  photoProfil: z.string().optional(),
});
