/**
 * Formate un nom de fichier en supprimant les caractères problématiques
 * Remplace les espaces et underscores par des tirets
 * @param fileName - Nom de fichier brut (ex: "image_2026.png" ou "my file.jpg")
 * @returns Nom formaté (ex: "image-2026.png" ou "my-file.jpg")
 */
export const formatFileName = (fileName: string): string => {
  if (!fileName) return fileName;

  // Extraire l'extension
  const lastDotIndex = fileName.lastIndexOf(".");
  let baseName = fileName;
  let extension = "";

  if (lastDotIndex !== -1) {
    baseName = fileName.substring(0, lastDotIndex);
    extension = fileName.substring(lastDotIndex);
  }

  // Remplacer les espaces et underscores par des tirets
  const formattedBaseName = baseName.replace(/[\s_]+/g, "-");

  return formattedBaseName + extension;
};

/**
 * Extrait le nom de base formaté d'un nomUnique renvoyé par le backend
 * Exemple: "deals/image-2026_1707988800000.png" -> "image-2026"
 * @param nomUnique - Nom complet avec répertoire et timestamp
 * @returns Nom de base sans timestamp et extension
 */
export const extractBaseNameFromNomUnique = (nomUnique: string): string => {
  if (!nomUnique) return "";

  // Récupérer la partie après le dernier /
  const lastSlashIndex = nomUnique.lastIndexOf("/");
  const fileNameWithTimestamp =
    lastSlashIndex !== -1 ? nomUnique.substring(lastSlashIndex + 1) : nomUnique;

  // Extraire le nom avant le timestamp (format: baseName_timestamp.ext)
  // On cherche le dernier underscore suivi de chiffres et d'un point
  const timestampPattern = /_\d+\./;
  const match = fileNameWithTimestamp.match(timestampPattern);

  if (match) {
    const baseName = fileNameWithTimestamp.substring(0, match.index);
    return baseName;
  }

  // Si pas de pattern trouvé, retourner le nom sans extension
  const lastDotIndex = fileNameWithTimestamp.lastIndexOf(".");
  return lastDotIndex !== -1
    ? fileNameWithTimestamp.substring(0, lastDotIndex)
    : fileNameWithTimestamp;
};
