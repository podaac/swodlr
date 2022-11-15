/* -- Load balancer -- */
resource "aws_lb" "app" {
  name = "${local.resource_prefix}-alb"
  load_balancer_type = "application"

  subnets = data.aws_subnets.private.ids
  security_groups = [aws_security_group.load_balancer.id]
}

resource "aws_lb_listener" "app" {
  load_balancer_arn = aws_lb.app.arn
  port = 80

  default_action {
    type = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }
}

resource "aws_lb_target_group" "app" {
  name = "${local.resource_prefix}-app-lb-tg"
  port = 80
  protocol = "HTTP"
  target_type = "ip"
  vpc_id = data.aws_vpc.default.id
}

/* -- Security Group -- */
resource "aws_security_group" "load_balancer" {
  vpc_id = data.aws_vpc.default.id
  name = "${local.resource_prefix}-lb-sg"

  ingress {
    from_port = 80
    to_port   = 80
    protocol  = "tcp"
    cidr_blocks    = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  egress {
    from_port    = 0
    to_port      = 0
    protocol     = "-1"
    cidr_blocks    = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }
}