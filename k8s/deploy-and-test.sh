#!/bin/bash

# Script de déploiement et test pour PayToGether
# Date: 22 février 2026

set -e

NAMESPACE="paytogether"
BFF_APP="bff"
CONNECT_APP="connect"

echo "=========================================="
echo "  PayToGether - Déploiement & Test"
echo "=========================================="

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction pour afficher les messages
info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Vérifier que kubectl est installé
if ! command -v kubectl &> /dev/null; then
    error "kubectl n'est pas installé"
    exit 1
fi

# Menu principal
echo ""
echo "Choisissez une action:"
echo "1) Déployer uniquement BFF"
echo "2) Déployer tous les services"
echo "3) Tester la configuration"
echo "4) Afficher les logs BFF"
echo "5) Afficher les logs Connect"
echo "6) Vérifier l'état des services"
echo "7) Redémarrer BFF"
echo "8) Test de connectivité BFF -> Connect"
echo "9) Quitter"
echo ""
read -p "Votre choix: " choice

case $choice in
    1)
        info "Déploiement du BFF..."
        kubectl apply -f deployment-bff.yaml
        info "Attente du déploiement..."
        kubectl rollout status deployment/bff-deploiement -n $NAMESPACE
        info "Déploiement terminé!"
        ;;

    2)
        info "Déploiement de tous les services..."
        kubectl apply -f namespace-paytogether.yaml
        kubectl apply -f service-connect.yaml
        kubectl apply -f deployment-connect.yaml
        kubectl apply -f service-bff.yaml
        kubectl apply -f deployment-bff.yaml

        info "Attente du déploiement Connect..."
        kubectl rollout status deployment/connect-deploiement -n $NAMESPACE

        info "Attente du déploiement BFF..."
        kubectl rollout status deployment/bff-deploiement -n $NAMESPACE

        info "Tous les services sont déployés!"
        ;;

    3)
        info "Test de la configuration..."

        echo ""
        info "1. Services:"
        kubectl get svc -n $NAMESPACE

        echo ""
        info "2. Déploiements:"
        kubectl get deployments -n $NAMESPACE

        echo ""
        info "3. Pods:"
        kubectl get pods -n $NAMESPACE

        echo ""
        info "4. Endpoints connect-service:"
        kubectl get endpoints connect-service -n $NAMESPACE

        echo ""
        info "5. Variable d'environnement API_AUTH_URL dans BFF:"
        POD_NAME=$(kubectl get pod -n $NAMESPACE -l app=$BFF_APP -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
        if [ -n "$POD_NAME" ]; then
            kubectl exec -n $NAMESPACE $POD_NAME -- env | grep API_AUTH_URL || warn "Variable API_AUTH_URL non trouvée"
        else
            warn "Aucun pod BFF trouvé"
        fi

        echo ""
        info "6. Test résolution DNS connect-service depuis BFF:"
        if [ -n "$POD_NAME" ]; then
            kubectl exec -n $NAMESPACE $POD_NAME -- nslookup connect-service 2>/dev/null || warn "nslookup non disponible"
        fi
        ;;

    4)
        info "Logs du BFF (100 dernières lignes)..."
        kubectl logs -n $NAMESPACE -l app=$BFF_APP --tail=100
        ;;

    5)
        info "Logs du service Connect (100 dernières lignes)..."
        kubectl logs -n $NAMESPACE -l app=$CONNECT_APP --tail=100
        ;;

    6)
        info "État des services..."

        echo ""
        echo "=== Services ==="
        kubectl get svc -n $NAMESPACE

        echo ""
        echo "=== Déploiements ==="
        kubectl get deployments -n $NAMESPACE

        echo ""
        echo "=== Pods ==="
        kubectl get pods -n $NAMESPACE -o wide

        echo ""
        echo "=== Endpoints ==="
        kubectl get endpoints -n $NAMESPACE
        ;;

    7)
        info "Redémarrage du BFF..."
        kubectl rollout restart deployment/bff-deploiement -n $NAMESPACE
        info "Attente du redémarrage..."
        kubectl rollout status deployment/bff-deploiement -n $NAMESPACE
        info "Redémarrage terminé!"
        ;;

    8)
        info "Test de connectivité BFF -> Connect..."
        POD_NAME=$(kubectl get pod -n $NAMESPACE -l app=$BFF_APP -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")

        if [ -z "$POD_NAME" ]; then
            error "Aucun pod BFF trouvé"
            exit 1
        fi

        info "Pod BFF: $POD_NAME"

        echo ""
        info "Test 1: Résolution DNS"
        kubectl exec -n $NAMESPACE $POD_NAME -- nslookup connect-service 2>/dev/null || warn "nslookup non disponible"

        echo ""
        info "Test 2: Connexion HTTP (si curl disponible)"
        kubectl exec -n $NAMESPACE $POD_NAME -- curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://connect-service:8092 2>/dev/null || warn "curl non disponible ou service inaccessible"

        echo ""
        info "Test 3: Variable d'environnement"
        kubectl exec -n $NAMESPACE $POD_NAME -- env | grep API_AUTH_URL
        ;;

    9)
        info "Au revoir!"
        exit 0
        ;;

    *)
        error "Choix invalide"
        exit 1
        ;;
esac

echo ""
info "Terminé!"

