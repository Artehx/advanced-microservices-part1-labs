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
