import React from 'react';
import ReactMarkdown from 'react-markdown';

interface ChatMessageContentProps {
  content: string;
}

export const ChatMessageContent: React.FC<ChatMessageContentProps> = ({ content }) => {
  return (
    <div className="chat-markdown text-slate-800">
      <ReactMarkdown
        components={{
          h1: ({ children }) => (
            <h1 className="text-sm sm:text-base font-extrabold text-slate-900 mt-3 mb-1.5 pb-1 border-b border-slate-100">
              {children}
            </h1>
          ),
          h2: ({ children }) => (
            <h2 className="text-xs sm:text-sm font-extrabold text-slate-900 mt-3 mb-1.5 pb-1 border-b border-slate-100">
              {children}
            </h2>
          ),
          h3: ({ children }) => (
            <h3 className="text-xs sm:text-sm font-bold text-slate-900 mt-3 mb-1.5 pb-1 border-b border-slate-100">
              {children}
            </h3>
          ),
          p: ({ children }) => (
            <p className="text-xs sm:text-sm leading-relaxed text-slate-800 mb-2 last:mb-0">
              {children}
            </p>
          ),
          ul: ({ children }) => (
            <ul className="space-y-1.5 my-2 pl-4 list-disc list-outside marker:text-brand-600 text-xs sm:text-sm">
              {children}
            </ul>
          ),
          ol: ({ children }) => (
            <ol className="space-y-2 my-2 pl-4 list-decimal list-outside marker:font-bold marker:text-brand-700 text-xs sm:text-sm">
              {children}
            </ol>
          ),
          li: ({ children }) => (
            <li className="leading-relaxed text-slate-800">
              {children}
            </li>
          ),
          strong: ({ children }) => (
            <strong className="font-bold text-slate-900">
              {children}
            </strong>
          ),
          em: ({ children }) => (
            <em className="italic text-slate-600 font-medium">
              {children}
            </em>
          ),
          hr: () => (
            <hr className="my-3 border-t border-slate-200/80" />
          ),
          blockquote: ({ children }) => (
            <blockquote className="border-l-2 border-brand-500 bg-brand-50/50 pl-3 py-1.5 my-2 rounded-r-xl text-xs sm:text-sm text-slate-700 italic">
              {children}
            </blockquote>
          ),
          code: ({ children }) => (
            <code className="px-1.5 py-0.5 rounded bg-slate-100 text-brand-800 font-mono text-[11px] border border-slate-200/60">
              {children}
            </code>
          ),
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
};
