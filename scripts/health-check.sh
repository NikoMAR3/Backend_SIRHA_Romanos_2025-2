#!/bin/bash
set -e

NAMESPACE="sirha"
MAX_RETRIES=10
RETRY_DELAY=10

echo "Verificando salud de la aplicación..."

for i in $(seq 1 $MAX_RETRIES); do
    echo "Intento $i/$MAX_RETRIES..."

    # Obtener nombre del pod
    POD_NAME=$(kubectl get pods -n $NAMESPACE -l app=sirha-api -o jsonpath='{.items[0].metadata.name}')

    if [ -z "$POD_NAME" ]; then
        echo "No se encontraron pods de sirha-api"
        exit 1
    fi

    # Ejecutar health check dentro del pod
    if kubectl exec -n $NAMESPACE $POD_NAME -- curl -f http://localhost:8080/actuator/health; then
        echo ""
        echo "Health check exitoso"
        exit 0
    fi

    echo "Esperando $RETRY_DELAY segundos..."
    sleep $RETRY_DELAY
done

echo "Health check falló después de $MAX_RETRIES intentos"
exit 1
