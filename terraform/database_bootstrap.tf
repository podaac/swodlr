/* -- AMI -- */
data "aws_ssm_parameter" "ami_id"{
    name = var.ami_id_ssm_name
}

/* -- Networking -- */
resource "aws_network_interface" "db_bootstrap" {
    subnet_id = values(data.aws_subnet.private)[0].id
    security_groups = [aws_security_group.database.id]
}

/* -- Boot config -- */
data "cloudinit_config" "db_bootstrap" {
    gzip = true

    part {
        content_type = "text/cloud-config"
        content = templatefile(
            "${path.module}/resources/bootstrap.yml.tftpl",
            {
                schema_sql = filebase64("${path.module}/../database/schema.sql")

                region = var.region
                db_name = var.db_name
                endpoint = aws_db_instance.database.endpoint

                admin_username = aws_ssm_parameter.admin_username.value
                admin_password = aws_ssm_parameter.admin_password.value

                app_username = aws_ssm_parameter.app_username.value
                app_password = aws_ssm_parameter.app_password.value
            }
        )
    }

    part {
        content_type = "text/x-shellscript"
        content = file("${path.module}/resources/init-db.sh")
    }
}

/* -- Initialization Check -- */
data "aws_ssm_parameters_by_path" "swodlr" {
    path = dirname(local.db_bootstrap_time_ssm_id)
}

locals {
    db_bootstrap_time_ssm_id = "/service/swodlr/db-bootstrap-time"
    db_initalized = contains(data.aws_ssm_parameters_by_path.swodlr.names, local.db_bootstrap_time_ssm_id)
}

/* -- EC2 -- */
resource "aws_instance" "db_bootstrap" {
    ami = data.aws_ssm_parameter.ami_id.value
    instance_type = "t3.micro"
    iam_instance_profile = "app-instance-default-instance-profile"
    instance_initiated_shutdown_behavior = "terminate"
    monitoring = false
    tags = {
        Name: "${local.resource_prefix}-db-bootstrap"
    }

    network_interface {
        network_interface_id = aws_network_interface.db_bootstrap.id
        device_index = 0
    }

    count = local.db_initalized ? 0 : 1
    depends_on = [aws_db_instance.database]
    user_data_base64 = data.cloudinit_config.db_bootstrap.rendered
}

/* -- Store bootstrap metadata -- */
resource "aws_ssm_parameter" "bootstrap_time" {
    name = "/service/swodlr/db-bootstrap-time"
    description = "Timestamp of when the database bootstrap instance was launched"
    depends_on = [aws_instance.db_bootstrap]
    type = "String"
    value = timestamp()

    lifecycle {
        ignore_changes = [value]
    }
}
