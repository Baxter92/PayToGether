import type { ReactElement } from "react";
import { useState } from "react";
import { Search, MoreHorizontal, Mail, Ban, Shield } from "lucide-react";
import { Button } from "@/common/components/ui/button";
import { Input } from "@/common/components/ui/input";
import { Card, CardContent, CardHeader } from "@/common/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/common/components/ui/table";
import { Badge } from "@/common/components/ui/badge";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/common/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/common/components/ui/dropdown-menu";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { useI18n } from "@/common/hooks/useI18n";

const mockUsers = [
  {
    id: 1,
    name: "Jean Dupont",
    email: "jean.dupont@email.com",
    avatar: "",
    orders: 12,
    spent: 450,
    role: "user",
    status: "active",
  },
  {
    id: 2,
    name: "Marie Martin",
    email: "marie.martin@email.com",
    avatar: "",
    orders: 8,
    spent: 320,
    role: "user",
    status: "active",
  },
  {
    id: 3,
    name: "Pierre Bernard",
    email: "pierre.bernard@email.com",
    avatar: "",
    orders: 25,
    spent: 890,
    role: "admin",
    status: "active",
  },
  {
    id: 4,
    name: "Sophie Laurent",
    email: "sophie.laurent@email.com",
    avatar: "",
    orders: 3,
    spent: 120,
    role: "user",
    status: "banned",
  },
  {
    id: 5,
    name: "Lucas Petit",
    email: "lucas.petit@email.com",
    avatar: "",
    orders: 15,
    spent: 560,
    role: "user",
    status: "active",
  }
];

export default function AdminUsers(): ReactElement {
  const [searchQuery, setSearchQuery] = useState("");
  const { t: tAdmin } = useI18n("admin");
  const { t: tRoles } = useI18n("roles");
  const { t: tStatus } = useI18n("status");

  const filteredUsers = mockUsers.filter(
    (user) =>
      user.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const getStatusBadge = (status: string): ReactElement => {
    switch (status) {
      case "active":
        return (
          <Badge className="bg-green-100 text-green-800 hover:bg-green-100">
            {tStatus("active")}
          </Badge>
        );
      case "banned":
        return (
          <Badge className="bg-destructive/10 text-destructive hover:bg-destructive/10">
            {tStatus("banned")}
          </Badge>
        );
      default:
        return <Badge>{tStatus(status)}</Badge>;
    }
  };

  const getRoleBadge = (role: string): ReactElement => {
    switch (role) {
      case "admin":
        return (
          <Badge className="bg-primary/10 text-primary hover:bg-primary/10">
            {tRoles("admin")}
          </Badge>
        );
      default:
        return <Badge variant="outline">{tRoles("user")}</Badge>;
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold font-heading">
          {tAdmin("users.title")}
        </h1>
        <p className="text-muted-foreground">{tAdmin("users.description")}</p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            <div className="relative flex-1 max-w-sm">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder={tAdmin("users.search")}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>{tAdmin("users.name")}</TableHead>
                <TableHead>{tAdmin("users.role")}</TableHead>
                <TableHead className="text-right">
                  {tAdmin("users.orders")}
                </TableHead>
                <TableHead className="text-right">
                  {tAdmin("users.spent")}
                </TableHead>
                <TableHead>{tAdmin("users.status")}</TableHead>
                <TableHead className="text-right">
                  {tAdmin("users.actions")}
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredUsers.map((user) => (
                <TableRow key={user.id}>
                  <TableCell>
                    <div className="flex items-center gap-3">
                      <Avatar className="h-8 w-8">
                        <AvatarImage src={user.avatar} />
                        <AvatarFallback>
                          {user.name
                            .split(" ")
                            .map((n) => n[0])
                            .join("")}
                        </AvatarFallback>
                      </Avatar>
                      <div>
                        <p className="font-medium">{user.name}</p>
                        <p className="text-sm text-muted-foreground">
                          {user.email}
                        </p>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>{getRoleBadge(user.role)}</TableCell>
                  <TableCell className="text-right">{user.orders}</TableCell>
                  <TableCell className="text-right">
                    {formatCurrency(user.spent)}
                  </TableCell>
                  <TableCell>{getStatusBadge(user.status)}</TableCell>
                  <TableCell className="text-right">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem>
                          <Mail className="h-4 w-4 mr-2" />
                          {tAdmin("users.sendEmail")}
                        </DropdownMenuItem>
                        <DropdownMenuItem>
                          <Shield className="h-4 w-4 mr-2" />
                          {tAdmin("users.makeAdmin")}
                        </DropdownMenuItem>
                        <DropdownMenuItem className="text-destructive">
                          <Ban className="h-4 w-4 mr-2" />
                          {tAdmin("users.banUser")}
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
