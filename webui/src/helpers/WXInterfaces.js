// WebUI X Interface bridges
export const fileInterface = typeof $azenith !== 'undefined' && $azenith?.file 
  ? $azenith.file 
  : typeof $encore !== 'undefined' && $encore?.file 
    ? $encore.file 
    : null

export const packageManagerInterface = typeof $azenith !== 'undefined' && $azenith?.packageManager 
  ? $azenith.packageManager 
  : typeof $encore !== 'undefined' && $encore?.packageManager 
    ? $encore.packageManager 
    : null

export const moduleInterface = typeof $azenith !== 'undefined' && $azenith?.module 
  ? $azenith.module 
  : typeof $encore !== 'undefined' && $encore?.module 
    ? $encore.module 
    : null
