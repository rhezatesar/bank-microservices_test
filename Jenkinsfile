pipeline {
    agent { label 'built-in' }

    environment {
        DB_URL = "jdbc:postgresql://postgres:5432/bankdb"
        DB_USER = "sonar"
        DB_PASS = "sonar123"
        DOCKER_NETWORK = "bindnamed_jenkins_network"
        SONAR_URL = "http://sonarqube:9000"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/ferilauw/bank-microservices.git'
            }
        }

        stage('Debug Workspace') {
            steps {
                sh '''
                echo "=== CURRENT DIR ==="
                pwd

                echo "=== ACCOUNT MIGRATION ==="
                ls -la account-service/db/migration || true

                echo "=== TRANSACTION MIGRATION ==="
                ls -la transaction-service/db/migration || true
                '''
            }
        }

        stage('Account Service Scan') {
            steps {
                dir('account-service') {
                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_AUTH_TOKEN')]) {
                        sh '''
                        echo "=== RUN FLYWAY ACCOUNT ==="

                        echo "=== LOCAL FILE ==="
                        ls -la db/migration
            
                        echo "=== INSIDE CONTAINER ==="
                        docker run --rm \
                        --network $DOCKER_NETWORK \
                        -v $PWD/db/migration:/flyway/sql \
                        busybox \
                        ls -la /flyway/sql
                        
                        docker run --rm \
                        --network $DOCKER_NETWORK \
                        -v $(pwd):/usr/src \
                        sonarsource/sonar-scanner-cli \
                        -Dsonar.projectKey=account-service \
                        -Dsonar.sources=. \
                        -Dsonar.host.url=$SONAR_URL \
                        -Dsonar.token=$SONAR_AUTH_TOKEN
                        '''
                    }
                }
            }
        }

        stage('Transaction Service Scan') {
            steps {
                dir('transaction-service') {
                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_AUTH_TOKEN')]) {
                        sh '''
                        docker run --rm \
                        --network $DOCKER_NETWORK \
                        -v $(pwd):/usr/src \
                        sonarsource/sonar-scanner-cli \
                        -Dsonar.projectKey=transaction-service \
                        -Dsonar.sources=. \
                        -Dsonar.host.url=$SONAR_URL \
                        -Dsonar.token=$SONAR_AUTH_TOKEN
                        '''
                    }
                }
            }
        }

        stage('DB Migration Account') {
            steps {
                dir('account-service') {
                    sh '''
                    echo "=== RUN FLYWAY ACCOUNT ==="

                    docker run --rm \
                    --network $DOCKER_NETWORK \
                    --volumes-from jenkins \
                    flyway/flyway \
                    -url=$DB_URL \
                    -user=$DB_USER \
                    -password=$DB_PASS \
                    -locations=filesystem:$WORKSPACE/account-service/db/migration \
                    migrate
                    '''
                }
            }
        }

        stage('DB Migration Transaction') {
            steps {
                dir('transaction-service') {
                    sh '''
                    docker run --rm \
                    --network $DOCKER_NETWORK \
                    --volumes-from jenkins \
                    flyway/flyway \
                    -url=$DB_URL \
                    -user=$DB_USER \
                    -password=$DB_PASS \
                    -locations=filesystem:$WORKSPACE/transaction-service/db/migration \
                    repair
                    '''
                    
                    sh '''
                    echo "=== RUN FLYWAY TRANSACTION ==="

                    docker run --rm \
                    --network $DOCKER_NETWORK \
                    --volumes-from jenkins \
                    flyway/flyway \
                    -url=$DB_URL \
                    -user=$DB_USER \
                    -password=$DB_PASS \
                    -locations=filesystem:$WORKSPACE/transaction-service/db/migration \
                    migrate
                    '''
                }
            }
        }
    }
}