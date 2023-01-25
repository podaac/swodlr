/* -- Cataloger -- */

// - KMS keys
resource "aws_kms_key" "catalog_sqs" {
  description = "${local.resource_prefix}-catalog-sqs"
  deletion_window_in_days = 10
}

resource "aws_kms_key" "catalog_sns" {
  description = "${local.resource_prefix}-catalog-sns"
  deletion_window_in_days = 10
}

// - SNS
resource "aws_sns_topic" "catalog" {
  name = "${local.resource_prefix}-catalog-topic"
  kms_master_key_id = aws_kms_key.catalog_sns.id
}

resource "aws_sns_topic_subscription" "catalog_sqs_target" {
  topic_arn = aws_sns_topic.catalog.arn
  protocol = "sqs"
  endpoint = "arn:aws:sqs:${var.region}:${data.aws_caller_identity.current.account_id}:${aws_sqs_queue.catalog.name}"
}

// - SQS
resource "aws_sqs_queue" "catalog" {
  name = "${local.resource_prefix}-catalog-queue"
  kms_master_key_id = aws_kms_key.catalog_sqs.id
  message_retention_seconds = 7 * 24 * 60 * 60  # 1 week
  visibility_timeout_seconds = 12 * 60 * 60     # 12 hours
}
