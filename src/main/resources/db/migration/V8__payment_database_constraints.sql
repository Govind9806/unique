-- Flyway Migration V8: Database-level Uniqueness Constraints for Payment Idempotency & Provider Transaction Mapping

ALTER TABLE payments ADD CONSTRAINT uk_payments_gateway_order_id UNIQUE (gateway_order_id);
ALTER TABLE ledger_transactions ADD CONSTRAINT uk_ledger_ref_type_id UNIQUE (reference_type, reference_id);
