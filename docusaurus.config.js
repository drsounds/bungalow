/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Bungalow',
  tagline: 'An open, plugin operating system for music',
  url: 'https://drsounds.github.io',
  baseUrl: process.env.BASE_URL || '/bungalow/',
  organizationName: 'drsounds',
  projectName: 'bungalow',
  onBrokenLinks: 'throw',
  markdown: {
    hooks: {
      onBrokenMarkdownLinks: 'throw',
    },
  },
  presets: [
    [
      'classic',
      {
        docs: {
          routeBasePath: '/',
          sidebarPath: require.resolve('./sidebars.js'),
          exclude: ['**/migrate-legacy-db.sql'],
        },
        blog: false,
      },
    ],
  ],
  themeConfig: {
    navbar: {
      title: 'Bungalow',
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'docs',
          position: 'left',
          label: 'Documentation',
        },
        {
          href: 'https://github.com/drsounds/bungalow',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Project',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/drsounds/bungalow',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Bungalow`,
    },
  },
};

module.exports = config;
