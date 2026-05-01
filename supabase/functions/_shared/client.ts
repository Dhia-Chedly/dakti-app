import { createClient, type SupabaseClient, type User } from "npm:@supabase/supabase-js@2";

const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
const supabaseServiceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";

function requireEnv(name: string, value: string): string {
  if (!value) {
    throw new Error(`Missing environment variable: ${name}`);
  }
  return value;
}

export function getBearerToken(req: Request): string | null {
  const header = req.headers.get("Authorization") ?? req.headers.get("authorization");
  if (!header) {
    return null;
  }
  const [scheme, token] = header.split(" ");
  if (scheme?.toLowerCase() !== "bearer" || !token) {
    return null;
  }
  return token;
}

export function createServiceClient(): SupabaseClient {
  return createClient(
    requireEnv("SUPABASE_URL", supabaseUrl),
    requireEnv("SUPABASE_SERVICE_ROLE_KEY", supabaseServiceRoleKey)
  );
}

export function createUserClient(token: string): SupabaseClient {
  return createClient(
    requireEnv("SUPABASE_URL", supabaseUrl),
    requireEnv("SUPABASE_ANON_KEY", supabaseAnonKey),
    {
      global: {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    }
  );
}

export async function requireUser(req: Request): Promise<User> {
  const token = getBearerToken(req);
  if (!token) {
    throw new Error("Missing bearer token");
  }

  const userClient = createUserClient(token);
  const { data, error } = await userClient.auth.getUser();
  if (error || !data.user) {
    throw new Error("Unauthorized request");
  }
  return data.user;
}