#!/bin/bash
set -e

NAMESPACE="sirha"
IMAGE_NAME="sirha-api:latest"

echo "Iniciando despliegue local de SIRHA..."

# 1. Verificar que kubectl esté disponible
if ! command -v kubectl &> /dev/null; then
    echo "kubectl no está instalado"
    exit 1
fi

# 2. Verificar contexto de Kubernetes
CONTEXT=$(kubectl config current-context)
echo "Usando contexto: $CONTEXT"

# 3. Crear namespace si no existe
kubectl get namespace $NAMESPACE &> /dev/null || kubectl create namespace $NAMESPACE

# 4. Aplicar manifiestos en orden
echo "Aplicando manifiestos de Kubernetes..."
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/mongodb-deployment.yaml
kubectl apply -f k8s/mongodb-service.yaml

# 5. Esperar a que MongoDB esté listo
echo "Esperando a que MongoDB esté listo..."
kubectl wait --for=condition=ready pod -l app=mongodb -n $NAMESPACE --timeout=120s

# 6. Desplegar API
echo "Desplegando SIRHA API..."
kubectl apply -f k8s/api-deployment.yaml
kubectl apply -f k8s/api-service.yaml

# 7. Esperar rollout
echo "Esperando rollout de la API..."
kubectl rollout status deployment/sirha-api -n $NAMESPACE --timeout=5m

# 8. Mostrar estado
echo ""
echo "Despliegue completado"
echo ""
kubectl get pods -n $NAMESPACE
echo ""
kubectl get svc -n $NAMESPACE

echo ""
echo "SIRHA desplegado exitosamente"
echo "Accede a la API en: http://localhost/swagger-ui/index.html"
