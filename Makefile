# ===============================================
# Makefile - PayToGether Docker Management
# ===============================================

.PHONY: help build run stop clean logs test push deploy-dev deploy-prod

# Variables
IMAGE_NAME := paytogether-bff
IMAGE_TAG := latest
REGISTRY := registry.dealtogether.ca
FULL_IMAGE := $(REGISTRY)/$(IMAGE_NAME):$(IMAGE_TAG)

# Couleurs pour l'affichage
GREEN := \033[0;32m
YELLOW := \033[1;33m
RED := \033[0;31m
NC := \033[0m # No Color

help: ## Affiche l'aide
	@echo "$(GREEN)PayToGether - Commandes Docker disponibles$(NC)"
	@echo "=============================================="
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  $(YELLOW)%-20s$(NC) %s\n", $$1, $$2}'

build: ## Build l'image Docker
	@echo "$(GREEN)🔨 Build de l'image Docker...$(NC)"
	DOCKER_BUILDKIT=1 docker build \
		-f modules/bff/Dockerfile \
		-t $(IMAGE_NAME):$(IMAGE_TAG) \
		-t $(FULL_IMAGE) \
		.
	@echo "$(GREEN)✅ Build terminé !$(NC)"

build-no-cache: ## Build l'image Docker sans cache
	@echo "$(GREEN)🔨 Build de l'image Docker (sans cache)...$(NC)"
	DOCKER_BUILDKIT=1 docker build \
		--no-cache \
		--progress=plain \
		-f modules/bff/Dockerfile \
		-t $(IMAGE_NAME):$(IMAGE_TAG) \
		-t $(FULL_IMAGE) \
		.
	@echo "$(GREEN)✅ Build terminé !$(NC)"

clean-buildkit: ## Nettoie le cache Docker BuildKit
	@echo "$(YELLOW)🧹 Nettoyage du cache BuildKit...$(NC)"
	docker builder prune -f
	@echo "$(GREEN)✅ Cache BuildKit nettoyé$(NC)"

rebuild: clean-buildkit build-no-cache ## Nettoie le cache et rebuild complètement
	@echo "$(GREEN)✅ Rebuild complet terminé !$(NC)"

run: ## Exécute le container
	@echo "$(GREEN)🚀 Démarrage du container...$(NC)"
	docker run -d \
		-p 8080:8080 \
		-e SPRING_PROFILES_ACTIVE=dev \
		--name $(IMAGE_NAME) \
		$(IMAGE_NAME):$(IMAGE_TAG)
	@echo "$(GREEN)✅ Container démarré : http://localhost:8080$(NC)"

run-compose: ## Démarre avec docker-compose
	@echo "$(GREEN)🚀 Démarrage de la stack complète...$(NC)"
	docker-compose up -d
	@echo "$(GREEN)✅ Stack démarrée !$(NC)"
	@echo "BFF: http://localhost:8080"
	@echo "MinIO Console: http://localhost:9001"

stop: ## Arrête le container
	@echo "$(YELLOW)🛑 Arrêt du container...$(NC)"
	docker stop $(IMAGE_NAME) 2>/dev/null || true
	docker rm $(IMAGE_NAME) 2>/dev/null || true
	@echo "$(GREEN)✅ Container arrêté$(NC)"

stop-compose: ## Arrête docker-compose
	@echo "$(YELLOW)🛑 Arrêt de la stack...$(NC)"
	docker-compose down
	@echo "$(GREEN)✅ Stack arrêtée$(NC)"

clean: stop ## Nettoie les containers et images
	@echo "$(YELLOW)🧹 Nettoyage...$(NC)"
	docker-compose down -v 2>/dev/null || true
	docker rmi $(IMAGE_NAME):$(IMAGE_TAG) 2>/dev/null || true
	docker rmi $(FULL_IMAGE) 2>/dev/null || true
	docker system prune -f
	@echo "$(GREEN)✅ Nettoyage terminé$(NC)"

logs: ## Affiche les logs
	@docker logs -f $(IMAGE_NAME)

logs-compose: ## Affiche les logs de la stack
	@docker-compose logs -f

health: ## Vérifie le health check
	@echo "$(GREEN)🔍 Vérification du health...$(NC)"
	@curl -f http://localhost:8080/actuator/health && \
		echo "$(GREEN)✅ Application healthy$(NC)" || \
		echo "$(RED)❌ Application unhealthy$(NC)"

test: ## Lance les tests
	@echo "$(GREEN)🧪 Tests de l'image...$(NC)"
	@docker run --rm \
		-e SPRING_PROFILES_ACTIVE=test \
		$(IMAGE_NAME):$(IMAGE_TAG) \
		sh -c "wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health" && \
		echo "$(GREEN)✅ Tests réussis$(NC)" || \
		echo "$(RED)❌ Tests échoués$(NC)"

push: build ## Push l'image vers le registry
	@echo "$(GREEN)📤 Push vers le registry...$(NC)"
	docker push $(FULL_IMAGE)
	@echo "$(GREEN)✅ Image pushée : $(FULL_IMAGE)$(NC)"

info: ## Affiche les infos de l'image
	@echo "$(GREEN)📊 Informations de l'image$(NC)"
	@echo "=============================================="
	@docker images $(IMAGE_NAME):$(IMAGE_TAG) --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"
	@echo ""
	@echo "Layers (10 premiers):"
	@docker history $(IMAGE_NAME):$(IMAGE_TAG) --format "table {{.CreatedBy}}\t{{.Size}}" | head -11

shell: ## Ouvre un shell dans le container
	@docker exec -it $(IMAGE_NAME) sh

deploy-dev: push ## Déploie en DEV (Kubernetes)
	@echo "$(GREEN)🚀 Déploiement en DEV...$(NC)"
	kubectl set image deployment/bff-deployment \
		bff=$(FULL_IMAGE) \
		--namespace=paytogether-dev
	kubectl rollout status deployment/bff-deployment \
		--namespace=paytogether-dev \
		--timeout=5m
	@echo "$(GREEN)✅ Déploiement DEV terminé$(NC)"

deploy-prod: push ## Déploie en PROD (Kubernetes)
	@echo "$(YELLOW)⚠️  Déploiement en PRODUCTION$(NC)"
	@echo "Êtes-vous sûr ? (y/N)" && read ans && [ $${ans:-N} = y ]
	@echo "$(GREEN)🚀 Déploiement en PROD...$(NC)"
	kubectl set image deployment/bff-deployment-prod \
		bff=$(FULL_IMAGE) \
		--namespace=paytogether-prod
	kubectl rollout status deployment/bff-deployment-prod \
		--namespace=paytogether-prod \
		--timeout=5m
	@echo "$(GREEN)✅ Déploiement PROD terminé$(NC)"

prune: ## Nettoie tout Docker (⚠️ dangereux)
	@echo "$(RED)⚠️  Nettoyage complet de Docker$(NC)"
	@echo "Êtes-vous sûr ? (y/N)" && read ans && [ $${ans:-N} = y ]
	docker system prune -a --volumes -f
	@echo "$(GREEN)✅ Nettoyage complet terminé$(NC)"

.DEFAULT_GOAL := help

