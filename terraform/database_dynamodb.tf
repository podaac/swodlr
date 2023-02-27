/* -- Ingest -- */
resource "aws_dynamodb_table" "ingest" {
  name = "${local.resource_prefix}-ingest"
  hash_key = "granule_id"
  billing_mode = "PAY_PER_REQUEST"

  attribute {
    name = "granule_id"
    type = "S"
  }
}
