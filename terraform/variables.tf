variable "app_name" {
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
    default = "	image_id_amz2"
    description = "Name of the SSM Parameter that contains the NGAP approved ECS AMI ID."
}
