/* -- AMI -- */
data "aws_ami" "amazn2-ami" {
    most_recent = true
    owners = ["amazon"]

    filter {
        name = "name"
        values = ["amzn2-ami-*"]
    }

    filter {
        name = "architecture"
        values = ["x86_64"]
    }
}

/* -- Networking -- */
locals {
    bootstrap_private_subnet = values(data.aws_subnet.private)
}

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
                bootstrap_db_sql = filebase64("${path.module}/../database/bootstrap_db.sql")

                region = var.region
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

/* -- EC2 -- */
resource "aws_instance" "db_bootstrap" {
    ami = data.aws_ami.amazn2-ami.id
    instance_type = "t2.nano"
    monitoring = false
    tags = {
        Name: "${local.resource_prefix}-db-bootstrap"
    }

    network_interface {
        network_interface_id = aws_network_interface.db_bootstrap.id
        device_index = 0
    }

    depends_on = [aws_db_instance.database]
    user_data_base64 = data.cloudinit_config.db_bootstrap.rendered
}
