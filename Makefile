.PHONY: preflight test up down verify logs up-extended verify-extended
preflight:
	bash scripts/preflight.sh
test:
	mvn -B test
up:
	docker compose up --build -d --wait
down:
	docker compose --profile extended down
verify:
	bash scripts/verify-baseline.sh
logs:
	docker compose logs --tail=50

up-extended:
	docker compose --profile extended up --build -d --wait
verify-extended:
	bash scripts/verify-extended.sh

.PHONY: p01-up p01-test p01-verify
p01-up:
	docker compose --profile extended up -d --build --wait inventory shipping orders api-gateway
p01-test:
	mvn -B -pl services/orders -am -Dtest=OrderEstimateExercise -Dsurefire.failIfNoSpecifiedTests=false test
p01-verify:
	bash scripts/verify-p01.sh

.PHONY: p02-test p02-verify
p02-test:
	mvn -B -pl services/orders -am -Dtest=InventoryResilienceExercise -Dsurefire.failIfNoSpecifiedTests=false test
p02-verify:
	bash scripts/verify-p02.sh

.PHONY: p03-up p03-verify p03-down
p03-up:
	bash scripts/p03-up.sh
p03-verify:
	bash scripts/verify-p03.sh
p03-down:
	docker compose -f docker-compose.yml -f compose.kafka.yml --profile extended down
