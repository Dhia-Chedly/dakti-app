import { GoogleGenAI } from "npm:@google/genai";

const defaultModel = Deno.env.get("GEMINI_MODEL") || "gemini-2.0-flash";

export async function generateGeminiText(prompt: string): Promise<string | null> {
  const apiKey = Deno.env.get("GEMINI_API_KEY");
  if (!apiKey) {
    return null;
  }

  try {
    const genAI = new GoogleGenAI({ apiKey });
    const response = await genAI.models.generateContent({
      model: defaultModel,
      contents: prompt
    });

    const text = response.text?.trim();
    return text && text.length > 0 ? text : null;
  } catch (error) {
    console.error("Gemini request failed; using fallback.", error);
    return null;
  }
}
