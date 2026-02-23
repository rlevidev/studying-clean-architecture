document.addEventListener('DOMContentLoaded', () => {
    console.log('JavaScript Vanilla carregado!');
    
    const jsDemo = document.getElementById('js-demo');
    const btnClick = document.getElementById('btn-click');
    
    if (jsDemo) {
        jsDemo.textContent = 'JavaScript Vanilla está funcionando corretamente!';
    }
    
    if (btnClick) {
        btnClick.addEventListener('click', () => {
            alert('Botão clicado! O frontend está integrado.');
        });
    }
});
