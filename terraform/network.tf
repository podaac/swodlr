data "aws_vpc" "default" {
    tags = {
        "Name": "Application VPC"
    }
}

data "aws_subnets" "private" {
    filter {
        name   = "vpc-id"
        values = [data.aws_vpc.default.id]
    }

    filter {
        name   = "tag:Name"
        values = ["Private application*"]
    }
}

data "aws_subnet" "private" {
    for_each = toset(data.aws_subnets.private.ids)
    id = each.key
    vpc_id = data.aws_vpc.default.id
}
