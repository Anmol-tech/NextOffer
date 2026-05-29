\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{resume}[2024/01/01 NextOffer resume class]

\LoadClass[10pt,letterpaper]{article}

\usepackage[left=0.6in,top=0.2in,right=0.6in,bottom=0.2in]{geometry}
\usepackage{array}
\usepackage{enumitem}
\usepackage{hyperref}

\pagestyle{empty}
\setlength{\parindent}{0pt}
\setlength{\parskip}{0pt}

\def\sectionlineskip{\vspace{1pt}}
\def\sectionskip{\vspace{4pt}}

% Do not wrap section body in a list environment — that shrinks \linewidth and breaks \hfill.
\newenvironment{rSection}[1]{%
  \sectionskip
  \noindent{\bfseries #1}\par
  \sectionlineskip
  \noindent\rule{\textwidth}{0.4pt}\par
  \vspace{3pt}%
}{%
  \vspace{4pt}%
}
