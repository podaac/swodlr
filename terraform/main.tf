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
    local = {
      source = "hashicorp/local"
      version = ">=2.2.3"
    }
  }
}

provider "aws" {
  region = var.region

  default_tags {
    tags = local.default_tags
  }

  ignore_tags {
    key_prefixes = ["gsfc-ngap"]
  }
}

data "aws_caller_identity" "current" {}

data "local_file" "build_gradle" {
  filename = abspath("${path.root}/../build.gradle")
}

locals {
  name    = var.app_name
  version = regex("version = '(\\S*)'", data.local_file.build_gradle.content)[0]
  environment = var.stage

  resource_prefix = "service-${local.name}-${local.environment}"
  
  service_path = "/service/${local.name}"
  app_path = "${local.service_path}/app"

  account_id = data.aws_caller_identity.current.account_id

  default_tags = length(var.default_tags) == 0 ? {
    team = "TVA"
    application = local.resource_prefix
    version = local.version
    Environment = local.environment
  } : var.default_tags
}
