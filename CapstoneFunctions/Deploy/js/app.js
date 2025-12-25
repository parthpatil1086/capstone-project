export async function loadPage(page, element) {
  document.querySelectorAll('.sidebar a').forEach(a => a.classList.remove('active'));
  element.classList.add('active');

  try {
    const html = await (await fetch(`pages/${page}.html`)).text();
    document.getElementById('content-area').innerHTML = html;
  } catch (err) {
    console.error(err);
    document.getElementById('content-area').innerHTML = `<p style="color:red;">Failed to load ${page}</p>`;
  }
}

window.loadPage = loadPage;

window.onload = () => {
  document.querySelector('.sidebar a.active').click();
};
