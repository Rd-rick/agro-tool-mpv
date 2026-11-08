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
import { ChatMessageContent } from './ChatMessageContent';

interface Message {
  id: string;
  sender: 'user' | 'assistant';
  text: string;
  tools?: { ferramenta: string; argumentos: Record<string, unknown> }[];
  sources?: string[];
}



export const FloatingChatbot: React.FC = () => {
  const { isConsultado, appliedFilters } = useFilter();

  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [messages, setMessages] = useState<Message[]>([
    {
      id: 'welcome',
      sender: 'assistant',
      text: 'Olá! Sou o Assistente Agro Tool. Analiso o risco climático (ZARC) e cotações da Epagri e CONAB para Santa Catarina. Como posso ajudar?',
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

    if (!isConsultado || !appliedFilters) {
      const guidanceMessage: Message = {
        id: (Date.now() + 1).toString(),
        sender: 'assistant',
        text: 'Olá! Para que eu possa analisar o Zoneamento Agrícola (ZARC), riscos climáticos e cotações para a sua lavoura, por favor selecione seu município, período de plantio e área nos filtros no topo da página e clique em **Consultar**.',
      };
      setMessages((prev) => [...prev, guidanceMessage]);
      return;
    }

    setLoading(true);

    try {
      const res: RespostaAssistenteResponse = await postPerguntaAssistente({
        texto: question,
        codigoIbge: appliedFilters.codigoIbge,
        nomeMunicipio: appliedFilters.nomeMunicipio,
        mesInicio: appliedFilters.mesInicio,
        mesFim: appliedFilters.mesFim,
        tipoSolo: appliedFilters.soloCodigo || undefined,
        areaHectares: parseFloat(appliedFilters.areaHa),
      });

      const assistantMessage: Message = {
        id: (Date.now() + 1).toString(),
        sender: 'assistant',
        text: res.resposta,
        tools: res.ferramentasUtilizadas,
        sources: res.fontesCitadas,
      };

      setMessages((prev) => [...prev, assistantMessage]);
    } catch (err: unknown) {
      const respData = (err as { response?: { data?: { message?: string } } })?.response?.data;
      const errorMsg =
        respData?.message ||
        'Estamos temporariamente indisponíveis. Por favor, tente novamente amanhã.';
      setMessages((prev) => [
        ...prev,
        {
          id: (Date.now() + 1).toString(),
          sender: 'assistant',
          text: errorMsg,
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
        text:
          isConsultado && appliedFilters
            ? `Conversa reiniciada. Como posso ajudar com sua lavoura em ${appliedFilters.nomeMunicipio}?`
            : 'Conversa reiniciada. Selecione seu município, período de plantio e área nos filtros no topo da página e clique em Consultar.',
      },
    ]);
  };

  const quickPrompts = [
    'Ver ranking de culturas',
    'Qual a melhor cultura hoje?',
    'Por que a soja supera o milho?',
    'Quais culturas têm menor risco?',
  ];

  return (
    <div className="fixed bottom-6 right-6 z-50 flex flex-col items-end">
      {isOpen && (
        <div className="w-[360px] sm:w-[450px] md:w-[480px] h-[540px] sm:h-[580px] max-h-[85vh] bg-white rounded-3xl shadow-2xl border border-slate-100 flex flex-col overflow-hidden animate-in fade-in slide-in-from-bottom-4 duration-200">
          <div className="p-4 border-b border-slate-100 bg-slate-50/70 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-10 h-10 rounded-2xl bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600 shadow-sm">
                <Bot className="w-5 h-5" />
              </div>
              <div>
                <h4 className="text-sm font-bold text-slate-900 flex items-center gap-1.5">
                  <span>Agro IA</span>
                  <Sparkles className="w-3.5 h-3.5 text-amber-500" />
                </h4>
                <span className="text-xs text-slate-500 font-medium">
                  {isConsultado && appliedFilters
                    ? `${appliedFilters.nomeMunicipio} - SC (${appliedFilters.areaHa} ha)`
                    : 'Santa Catarina'}
                </span>
              </div>
            </div>

            <div className="flex items-center gap-1">
              <button
                type="button"
                onClick={handleReset}
                className="p-2 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors cursor-pointer"
                title="Reiniciar conversa"
              >
                <RotateCcw className="w-4 h-4" />
              </button>
              <button
                type="button"
                onClick={() => setIsOpen(false)}
                className="p-2 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors cursor-pointer"
                title="Fechar chat"
                aria-label="Fechar chat"
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
                  className={`rounded-2xl px-3.5 py-2.5 text-xs sm:text-sm leading-relaxed ${
                    m.sender === 'user'
                      ? 'max-w-[85%] bg-brand-600 text-white font-medium rounded-tr-none'
                      : 'max-w-[96%] w-full bg-slate-50 border border-slate-100 text-slate-800 rounded-tl-none shadow-xs'
                  }`}
                >
                  {m.sender === 'user' ? (
                    <p className="whitespace-pre-line">{m.text}</p>
                  ) : (
                    <ChatMessageContent content={m.text} />
                  )}

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
                <span>Analisando dados agrícolas...</span>
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

      {!isOpen && (
        <button
          type="button"
          onClick={() => setIsOpen(true)}
          className="w-14 h-14 rounded-full bg-brand-600 hover:bg-brand-700 active:scale-95 text-white shadow-xl flex items-center justify-center transition-all hover:scale-105 relative cursor-pointer group animate-in fade-in duration-150"
          aria-label="Abrir assistente inteligente de IA"
          title="Assistente Agro IA"
        >
          <MessageSquare className="w-6 h-6" />
          <span className="absolute -top-1 -right-1 w-5 h-5 bg-amber-400 text-slate-900 rounded-full flex items-center justify-center text-[10px] font-black shadow-sm">
            <Sparkles className="w-3 h-3" />
          </span>
        </button>
      )}
    </div>
  );
};
