terraform {
    required_version = ">=1.2.7"

    backend "s3" {
        key = "services/swodlr/terraform.tfstate"
    }

    required_providers {
        aws = {
            source = "hashicorp/aws"
            version = ">=4.27.0"
        }
        random = {
            source = "hashicorp/random"
            version = ">=3.3.2"
        }
        cloudinit = {
            source = "hashicorp/cloudinit"
            version = ">=2.2.0"
        }
    }
}

provider "aws" {
    region = var.region

    default_tags {
      tags = local.default_tags
    }
}

data "aws_caller_identity" "current" {}

locals {
    name        = var.app_name
    environment = var.stage

    resource_prefix = "service-${local.name}-${local.environment}"

    default_tags = length(var.default_tags) == 0 ? {
        team = "TVA"
        application = local.resource_prefix
        Environment = local.environment
    } : var.default_tags
}
