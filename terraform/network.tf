data "aws_vpc" "default" {
    id = var.vpc_id
}

data "aws_subnet" "private" {
    for_each = toset(var.private_subnets)
    id = each.key
    vpc_id = data.aws_vpc.default.id
}
