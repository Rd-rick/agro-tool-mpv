import React, { useState, useRef, useEffect } from 'react';
import {
  Bot,
  Send,
  Sparkles,
  RotateCcw,
  Wrench,
  BookOpen,
  Loader2,
  X,
  MessageSquare,
} from 'lucide-react';
import { useFilter } from '../../context/FilterContext';
import { postPerguntaAssistente } from '../../api/endpoints';
import type { RespostaAssistenteResponse } from '../../types/api';

interface Message {
  id: string;
  sender: 'user' | 'assistant';
  text: string;
  tools?: { ferramenta: string; argumentos: Record<string, unknown> }[];
  sources?: string[];
}

export const FloatingChatbot: React.FC = () => {
  const { codigoIbge, nomeMunicipio, mesInicio, mesFim, soloCodigo, areaHa } = useFilter();

  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [messages, setMessages] = useState<Message[]>([
    {
      id: 'welcome',
      sender: 'assistant',
      text: `Olá! Sou seu Assistente Agro Tool. Posso analisar zoneamento de risco climático (ZARC) e cotações da Epagri e CONAB para ${nomeMunicipio || 'Santa Catarina'}. Como posso ajudar?`,
      sources: ['ZARC MAPA', 'Epagri/Cepa', 'CONAB'],
    },
  ]);

  const [input, setInput] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [messages, loading, isOpen]);

  const handleSend = async (textToSend?: string) => {
    const question = (textToSend || input).trim();
    if (!question || loading) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      sender: 'user',
      text: question,
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setLoading(true);

    try {
      const res: RespostaAssistenteResponse = await postPerguntaAssistente({
        texto: question,
        codigoIbge: codigoIbge || '4204809',
        mesInicio: mesInicio || undefined,
        mesFim: mesFim || undefined,
        tipoSolo: soloCodigo || undefined,
        areaHectares: areaHa ? parseFloat(areaHa) : undefined,
      });

      const assistantMessage: Message = {
        id: (Date.now() + 1).toString(),
        sender: 'assistant',
        text: res.resposta,
        tools: res.ferramentasUtilizadas,
        sources: res.fontesCitadas,
      };

      setMessages((prev) => [...prev, assistantMessage]);
    } catch {
      setMessages((prev) => [
        ...prev,
        {
          id: (Date.now() + 1).toString(),
          sender: 'assistant',
          text: 'Desculpe, ocorreu uma falha ao consultar o assistente. Verifique se o backend está ativo ou tente novamente.',
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setMessages([
      {
        id: 'welcome-reset',
        sender: 'assistant',
        text: `Conversa reiniciada. Pergunte sobre riscos ZARC, oportunidades ou histórico de preços para ${nomeMunicipio || 'sua região'}.`,
      },
    ]);
  };

  const quickPrompts = [
    'Qual a melhor cultura hoje?',
    'Por que a soja supera o milho?',
    'Quais culturas têm menor risco?',
  ];

  return (
    <div className="fixed bottom-6 right-6 z-50 flex flex-col items-end">
      {isOpen && (
        <div className="mb-3 w-[360px] sm:w-[420px] h-[520px] max-h-[80vh] bg-white rounded-3xl shadow-2xl border border-slate-100 flex flex-col overflow-hidden animate-in fade-in slide-in-from-bottom-4 duration-200">
          <div className="p-4 border-b border-slate-100 bg-slate-50/70 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-10 h-10 rounded-2xl bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600 shadow-sm">
                <Bot className="w-5 h-5" />
              </div>
              <div>
                <h4 className="text-sm font-bold text-slate-900 flex items-center gap-1.5">
                  <span>Assistente Agro IA</span>
                  <Sparkles className="w-3.5 h-3.5 text-amber-500" />
                </h4>
                <span className="text-xs text-slate-500 font-medium">
                  {nomeMunicipio ? `${nomeMunicipio} - SC` : 'Santa Catarina'}
                </span>
              </div>
            </div>

            <div className="flex items-center gap-1">
              <button
                type="button"
                onClick={handleReset}
                className="p-2 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors"
                title="Reiniciar conversa"
              >
                <RotateCcw className="w-4 h-4" />
              </button>
              <button
                type="button"
                onClick={() => setIsOpen(false)}
                className="p-2 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors"
                title="Minimizar chat"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          <div className="flex-1 overflow-y-auto p-4 space-y-3">
            {messages.map((m) => (
              <div
                key={m.id}
                className={`flex flex-col ${m.sender === 'user' ? 'items-end' : 'items-start'}`}
              >
                <div
                  className={`max-w-[88%] rounded-2xl px-3.5 py-2.5 text-xs sm:text-sm leading-relaxed ${
                    m.sender === 'user'
                      ? 'bg-brand-600 text-white font-medium rounded-tr-none'
                      : 'bg-slate-50 border border-slate-100 text-slate-800 rounded-tl-none'
                  }`}
                >
                  <p className="whitespace-pre-line">{m.text}</p>

                  {m.tools && m.tools.length > 0 && (
                    <div className="mt-2 pt-2 border-t border-slate-200/60 flex flex-wrap gap-1.5">
                      {m.tools.map((t, idx) => (
                        <span
                          key={idx}
                          className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-brand-100/60 text-brand-800 text-xs font-semibold"
                        >
                          <Wrench className="w-3 h-3" />
                          {t.ferramenta}
                        </span>
                      ))}
                    </div>
                  )}

                  {m.sources && m.sources.length > 0 && (
                    <div className="mt-1.5 flex flex-wrap gap-2">
                      {m.sources.map((s, idx) => (
                        <span
                          key={idx}
                          className="inline-flex items-center gap-1 text-xs text-slate-500 font-medium"
                        >
                          <BookOpen className="w-3 h-3 text-slate-400" />
                          {s}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            ))}

            {loading && (
              <div className="flex items-center gap-2 text-xs text-slate-600 font-medium py-1">
                <Loader2 className="w-4 h-4 animate-spin text-brand-600" />
                <span>Consultando motor de decisão...</span>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <div className="p-3 border-t border-slate-100 bg-white">
            <div className="flex items-center gap-1.5 overflow-x-auto pb-2 scrollbar-none">
              {quickPrompts.map((p, idx) => (
                <button
                  key={idx}
                  type="button"
                  onClick={() => handleSend(p)}
                  disabled={loading}
                  className="px-3 py-1 rounded-full bg-slate-50 hover:bg-brand-50 hover:text-brand-800 text-slate-700 text-xs font-medium whitespace-nowrap border border-slate-200/80 transition-colors shrink-0"
                >
                  {p}
                </button>
              ))}
            </div>

            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSend();
              }}
              className="relative flex items-center mt-1"
            >
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder="Pergunte sobre plantio, riscos ou preços..."
                disabled={loading}
                className="w-full pl-3.5 pr-11 py-2.5 text-xs sm:text-sm bg-slate-50 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 text-slate-800 placeholder:text-slate-400"
              />
              <button
                type="submit"
                disabled={!input.trim() || loading}
                className="absolute right-1.5 w-8 h-8 rounded-xl bg-brand-600 text-white flex items-center justify-center hover:bg-brand-700 disabled:opacity-40 transition-colors shadow-sm"
              >
                <Send className="w-4 h-4" />
              </button>
            </form>
          </div>
        </div>
      )}

      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="w-14 h-14 rounded-full bg-brand-600 hover:bg-brand-700 active:scale-95 text-white shadow-xl flex items-center justify-center transition-all hover:scale-105 relative cursor-pointer group"
        aria-label="Abrir assistente inteligente de IA"
        title="Assistente Agro IA"
      >
        {isOpen ? (
          <X className="w-6 h-6" />
        ) : (
          <>
            <MessageSquare className="w-6 h-6" />
            <span className="absolute -top-1 -right-1 w-5 h-5 bg-amber-400 text-slate-900 rounded-full flex items-center justify-center text-[10px] font-black shadow-sm">
              <Sparkles className="w-3 h-3" />
            </span>
          </>
        )}
      </button>
    </div>
  );
};
