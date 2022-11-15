variable "app_name" {
    default = "swodlr"
    type = string
}

variable "db_name" {
    default = "swodlr"
    type = string
}

variable "default_tags" {
    type = map(string)
    default = {}
}

variable "stage" {
    type = string
}

variable "region" {
    type = string
}

variable "ami_id_ssm_name" {
    default = "image_id_amz2"
    description = "Name of the SSM Parameter that contains the NGAP approved ECS AMI ID."
}

variable "container_image" {
    type = string
    default = "ghcr.io/podaac/swodlr-api"
}

variable "container_image_tag" {
    type = string
}

variable "edl_client_id" {
    type = string
}

variable "edl_client_secret" {
    type = string
}

variable "session_encryption_key" {
    type = string
}
