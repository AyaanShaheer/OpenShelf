locals {
  name_prefix = "openshelf-demo"

  azs = slice(
    data.aws_availability_zones.available.names,
    0,
    2
  )
}

# ------------------------------------------------
# 1. VPC
# ------------------------------------------------

resource "aws_vpc" "openshelf" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "${local.name_prefix}-vpc"
  }
}

# ------------------------------------------------
# 2. Public subnets
# ------------------------------------------------

resource "aws_subnet" "public" {
  for_each = toset(local.azs)

  vpc_id            = aws_vpc.openshelf.id
  availability_zone = each.key
  cidr_block = cidrsubnet(
    var.vpc_cidr,
    8,
    index(local.azs, each.key) + 1
  )
  map_public_ip_on_launch = true

  tags = {
    Name = "${local.name_prefix}-public-${each.key}"
    Tier = "Public"
  }
}

# ------------------------------------------------
# 3. Private subnets
# ------------------------------------------------

resource "aws_subnet" "private" {
  for_each = toset(local.azs)

  vpc_id            = aws_vpc.openshelf.id
  availability_zone = each.key
  cidr_block = cidrsubnet(
    var.vpc_cidr,
    8,
    index(local.azs, each.key) + 11
  )

  tags = {
    Name = "${local.name_prefix}-private-${each.key}"
    Tier = "Private"
  }
}

# ------------------------------------------------
# 4. Internet Gateway
# ------------------------------------------------

resource "aws_internet_gateway" "openshelf" {
  vpc_id = aws_vpc.openshelf.id

  tags = {
    Name = "${local.name_prefix}-igw"
  }
}

# ------------------------------------------------
# 5. Public route table
# ------------------------------------------------

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.openshelf.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.openshelf.id
  }

  tags = {
    Name = "${local.name_prefix}-public-rt"
  }
}

resource "aws_route_table_association" "public" {
  for_each = aws_subnet.public

  subnet_id      = each.value.id
  route_table_id = aws_route_table.public.id
}

# ------------------------------------------------
# 6. Private route table
# No NAT Gateway or internet route.
# ------------------------------------------------

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.openshelf.id

  tags = {
    Name = "${local.name_prefix}-private-rt"
  }
}

resource "aws_route_table_association" "private" {
  for_each = aws_subnet.private

  subnet_id      = each.value.id
  route_table_id = aws_route_table.private.id
}

# ------------------------------------------------
# 7. EC2 / K3s security group
# ------------------------------------------------

resource "aws_security_group" "openshelf_node" {
  name        = "${local.name_prefix}-node-sg"
  description = "Security group for OpenShelf EC2 K3s node"
  vpc_id      = aws_vpc.openshelf.id

  tags = {
    Name = "${local.name_prefix}-node-sg"
  }
}

# SSH: administrator IP only


# HTTP: public web traffic

resource "aws_vpc_security_group_ingress_rule" "http" {
  security_group_id = aws_security_group.openshelf_node.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 80
  to_port           = 80
  ip_protocol       = "tcp"
  description       = "Public HTTP"
}

# HTTPS: public secure web traffic

resource "aws_vpc_security_group_ingress_rule" "https" {
  security_group_id = aws_security_group.openshelf_node.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 443
  to_port           = 443
  ip_protocol       = "tcp"
  description       = "Public HTTPS"
}

# Outbound traffic: restricted to what the node actually needs
# (public HTTPS to container registries, DNS, and apt for the Ubuntu bootstrap).

resource "aws_vpc_security_group_egress_rule" "outbound_https" {
  security_group_id = aws_security_group.openshelf_node.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 443
  to_port           = 443
  ip_protocol       = "tcp"
  description       = "Outbound HTTPS to public registries"
}

resource "aws_vpc_security_group_egress_rule" "outbound_dns_udp" {
  security_group_id = aws_security_group.openshelf_node.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 53
  to_port           = 53
  ip_protocol       = "udp"
  description       = "Outbound DNS (UDP)"
}

resource "aws_vpc_security_group_egress_rule" "outbound_dns_tcp" {
  security_group_id = aws_security_group.openshelf_node.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 53
  to_port           = 53
  ip_protocol       = "tcp"
  description       = "Outbound DNS (TCP)"
}

resource "aws_vpc_security_group_egress_rule" "outbound_apt" {
  security_group_id = aws_security_group.openshelf_node.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 80
  to_port           = 80
  ip_protocol       = "tcp"
  description       = "Outbound HTTP for Ubuntu bootstrap (apt)"
}