/* -- Security Group -- */
resource "aws_security_group" "database" {
    vpc_id = data.aws_vpc.default.id
    name = "${local.resource_prefix}-sg"

    ingress {
        from_port = 5432
        to_port   = 5432
        protocol  = "tcp"
        self      = true
    }

    egress {
        from_port        = 0
        to_port          = 0
        protocol         = "-1"
        cidr_blocks      = ["0.0.0.0/0"]
        ipv6_cidr_blocks = ["::/0"]
    }
}

/* -- Subnet Group -- */
resource "aws_db_subnet_group" "default" {
    name = "${local.resource_prefix}-subnet"
    subnet_ids = [for k, v in data.aws_subnet.private : v.id]
}

/* -- Authentication Credentials -- */
resource "random_pet" "admin_username" {
    separator = "_"
}

resource "random_pet" "app_username" {
    separator = "_"
}

resource "random_password" "admin_password" {
    length = 32
    override_special = "!#$%&*()-_=+[]{}<>?"
}

resource "random_password" "app_password" {
    length = 32
    override_special = "!#$%&*()-_=+[]{}<>?"
}

/* -- Database -- */
resource "aws_db_instance" "database" {
    identifier = "${local.resource_prefix}-rds"

    instance_class = "db.t3.micro"
    allocated_storage = 20
    storage_type = "gp2"
    skip_final_snapshot = true
    multi_az = true

    vpc_security_group_ids = [aws_security_group.database.id]
    db_subnet_group_name = aws_db_subnet_group.default.id

    engine = "postgres"
    engine_version = "14.2"

    username = random_pet.admin_username.id
    password = random_password.admin_password.result
}

/* -- SSM Parameter Store -- */
resource "aws_ssm_parameter" "admin_username" {
    name = "${local.resource_prefix}-admin"
    type = "String"
    value = aws_db_instance.database.username
    overwrite = true
}

resource "aws_ssm_parameter" "admin_password" {
    name = "${local.resource_prefix}-admin-pass"
    type = "SecureString"
    value = aws_db_instance.database.password
    overwrite = true
}

resource "aws_ssm_parameter" "app_username" {
    name = "${local.resource_prefix}-app"
    type = "String"
    value = random_pet.app_username.id
    overwrite = true
}

resource "aws_ssm_parameter" "app_password" {
    name = "${local.resource_prefix}-app-pass"
    type = "SecureString"
    value = random_password.app_password.result
    overwrite = true
}
